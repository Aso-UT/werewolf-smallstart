package werewolf

import werewolf.game.*
import werewolf.human.HumanPlayer
import werewolf.human.HumanIO
import werewolf.view.ChoiceView
import werewolf.phase.Conclave
import werewolf.phase.Epilogue
import werewolf.phase.OpenDiscussion

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HumanPlayerEpilogueTest {

    private class SpeakingIO : HumanIO {
        var capturedChronicles: List<ChronicleView> = emptyList()
        override fun display(view: RecallView) {}
        override fun promptFreeText(title: String, description: String): String = "human speaks"
        override fun promptChoice(view: ChoiceView): String = view.options.first()
        override fun watchEpilogue(chronicles: List<ChronicleView>) { capturedChronicles = chronicles }
    }

    private class SpeakingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun onReceive(event: GameEvent) {}
        override fun watchEpilogue(chronicles: List<ChronicleView>) {}
        override fun speak(context: DiscussionContext): Claim =
            Claim(this, context, Statement.Plain("$name speaks"), "intent")
    }

    @Test
    fun `claims from discussion appear in epilogue but StatementMade does not`() {
        val io = SpeakingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val speaker = SpeakingPlayer(Role.VILLAGER, "Speaker")
        val setup = TestLodge(human to Role.VILLAGER, speaker to Role.VILLAGER).create()

        OpenDiscussion(setup.playerManager, day = 1).conduct()
        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        assertTrue(io.capturedChronicles.any { it is ChronicleView.Action && it.actor == "Speaker" && it.category == "議論" })
        assertFalse(io.capturedChronicles.any { it is ChronicleView.Observation && it.category == "発言" })
    }

    @Test
    fun `claims from conclave appear in epilogue but WerewolfStatementMade does not`() {
        val io = SpeakingIO()
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)
        val wolf = SpeakingPlayer(Role.WEREWOLF, "Wolf")
        val setup = TestLodge(human to Role.WEREWOLF, wolf to Role.WEREWOLF).create()

        Conclave(setup.oracle, setup.playerManager, day = 1).conduct()
        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        assertTrue(io.capturedChronicles.any { it is ChronicleView.Action && it.actor == "Wolf" && it.category == "密談" })
        assertFalse(io.capturedChronicles.any { it is ChronicleView.Observation && it.category == "密談" })
    }
}
