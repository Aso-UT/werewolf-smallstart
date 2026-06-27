package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.MediumResult
import werewolf.game.RecallView
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.human.HumanIO
import werewolf.human.HumanPlayer
import werewolf.view.ChoiceView

class HumanPlayerTest {

    private class CapturingIO(
        vararg choiceAnswers: String,
        private val freeTextAnswer: String = "",
    ) : HumanIO {
        private val queue = ArrayDeque(choiceAnswers.toList())
        val promptedChoices = mutableListOf<ChoiceView>()
        var capturedChronicles: List<ChronicleView> = emptyList()

        override fun display(view: RecallView) {}
        override fun promptChoice(view: ChoiceView): String {
            promptedChoices += view
            return queue.removeFirst()
        }
        override fun promptFreeText(title: String, description: String): String = freeTextAnswer
        override fun watchEpilogue(chronicles: List<ChronicleView>) { capturedChronicles = chronicles }
    }

    @Test
    fun `choose presents non-self candidates as names and returns matching player`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO("Alice")
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val context = SelectionContext.Vote(human, listOf(human, alice))

        val selected = human.selectTarget(context)

        assertEquals(alice, selected)
        assertEquals(listOf("Alice"), io.promptedChoices.single().options)
    }

    @Test
    fun `speak in conclave uses PLAIN without prompting for type`() {
        val io = CapturingIO(freeTextAnswer = "hello")
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)
        val context = DiscussionContext.Conclave(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.Plain("hello"), statement)
        assertTrue(io.promptedChoices.isEmpty())
    }

    @Test
    fun `speak in open discussion prompts for type then returns plain statement`() {
        val io = CapturingIO(StatementType.PLAIN.displayName, freeTextAnswer = "hello")
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.Plain("hello"), statement)
        assertTrue(io.promptedChoices.single().options.contains(StatementType.PLAIN.displayName))
    }

    @Test
    fun `speak with DIVINATION_REPORT prompts for target and result`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO(
            StatementType.DIVINATION_REPORT.displayName,
            "Alice",
            DivineResult.WEREWOLF.displayName,
        )
        val human = HumanPlayer(Role.SEER, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human, alice))

        val statement = human.discuss(context)

        assertEquals(Statement.DivinationReport(human, alice, DivineResult.WEREWOLF), statement)
        assertEquals(3, io.promptedChoices.size)
    }

    @Test
    fun `speak with MEDIUM_REPORT prompts for target and result`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO(
            StatementType.MEDIUM_REPORT.displayName,
            "Alice",
            MediumResult.NOT_WEREWOLF.displayName,
        )
        val human = HumanPlayer(Role.MEDIUM, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human, alice))

        val statement = human.discuss(context)

        assertEquals(Statement.MediumReport(human, alice, MediumResult.NOT_WEREWOLF), statement)
        assertEquals(3, io.promptedChoices.size)
    }

    @Test
    fun `watchEpilogue delegates to io`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val chronicles = listOf(ChronicleView.Observation("Human", "発言", "hello"))

        human.watchEpilogue(chronicles)

        assertEquals(chronicles, io.capturedChronicles)
    }
}
