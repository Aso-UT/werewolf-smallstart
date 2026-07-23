package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import werewolf.game.AllPlayers
import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.MediumResult
import werewolf.game.RecallView
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Side
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.TimeOfDay
import werewolf.game.Wolves
import werewolf.human.HumanPlayer
import werewolf.view.Atmosphere
import werewolf.view.ChoiceView
import werewolf.view.DivinationView
import werewolf.view.EpilogueMood
import werewolf.view.PlayerStatusView
import werewolf.view.SelectionMood

class HumanPlayerTest {

    private class CapturingIO(
        vararg choiceAnswers: String,
        private val freeTextAnswer: String = "",
    ) : NothingHumanIO() {
        private val queue = ArrayDeque(choiceAnswers.toList())
        val promptedChoices = mutableListOf<ChoiceView>()
        val displayedViews = mutableListOf<RecallView>()
        val capturedAtmospheres = mutableListOf<Atmosphere>()
        val capturedSelectionMoods = mutableListOf<SelectionMood>()
        val capturedEpilogueMoods = mutableListOf<EpilogueMood>()
        var capturedChronicles: List<ChronicleView> = emptyList()

        override fun display(view: RecallView) { displayedViews += view }
        override fun updatePlayerStatusPanel(view: PlayerStatusView) {}
        override fun updateDivinationPanel(view: DivinationView) {}
        override fun updateAtmosphere(atmosphere: Atmosphere) { capturedAtmospheres += atmosphere }
        override fun updateSelectionMood(mood: SelectionMood) { capturedSelectionMoods += mood }
        override fun updateEpilogueMood(mood: EpilogueMood) { capturedEpilogueMoods += mood }
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
        val bob = NothingPlayer(Role.VILLAGER, "Bob")
        val io = CapturingIO("Alice")
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val context = SelectionContext.Vote(human, listOf(human, alice, bob))

        val selected = human.selectTarget(context)

        assertEquals(alice, selected)
        assertEquals(listOf("Alice", "Bob"), io.promptedChoices.single().options)
    }

    @Test
    fun `choose auto-selects without prompting when only one candidate exists`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val context = SelectionContext.Vote(human, listOf(human, alice))

        val selected = human.selectTarget(context)

        assertEquals(alice, selected)
        assertTrue(io.promptedChoices.isEmpty())
    }

    @Test
    fun `choose displays the selected player via io`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO("Alice")
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val context = SelectionContext.Vote(human, listOf(human, alice))

        human.selectTarget(context)

        assertEquals(
            listOf<RecallView>(RecallView.SelfAction("投票", "Alice", "プレイヤーが選択")),
            io.displayedViews,
        )
    }

    @Test
    fun `choose with Attack context updates selection mood to ATTACK`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO("Alice")
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)
        val context = SelectionContext.Attack(human, listOf(human, alice), emptyList())

        human.selectTarget(context)

        assertEquals(listOf(SelectionMood.ATTACK), io.capturedSelectionMoods)
    }

    @Test
    fun `choose with Divine context updates selection mood to DIVINE`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO("Alice")
        val human = HumanPlayer(Role.SEER, "Human", io)
        val context = SelectionContext.Divine(human, listOf(human, alice), emptyList())

        human.selectTarget(context)

        assertEquals(listOf(SelectionMood.DIVINE), io.capturedSelectionMoods)
    }

    @Test
    fun `choose with Guard context updates selection mood to GUARD`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO("Alice")
        val human = HumanPlayer(Role.HUNTER, "Human", io)
        val context = SelectionContext.Guard(human, listOf(human, alice))

        human.selectTarget(context)

        assertEquals(listOf(SelectionMood.GUARD), io.capturedSelectionMoods)
    }

    @Test
    fun `choose with Vote context updates selection mood to VOTE`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = CapturingIO("Alice")
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val context = SelectionContext.Vote(human, listOf(human, alice))

        human.selectTarget(context)

        assertEquals(listOf(SelectionMood.VOTE), io.capturedSelectionMoods)
    }

    @Test
    fun `speak in conclave uses PLAIN without prompting for type`() {
        val io = CapturingIO(freeTextAnswer = "hello")
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)
        val context = DiscussionContext.Conclave(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.Plain(human, "hello"), statement)
        assertTrue(io.promptedChoices.isEmpty())
    }

    @Test
    fun `speak in open discussion prompts for type then returns plain statement`() {
        val io = CapturingIO(StatementType.PLAIN.displayName, freeTextAnswer = "hello")
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.Plain(human, "hello"), statement)
        assertTrue(io.promptedChoices.single().options.contains(StatementType.PLAIN.displayName))
    }

    @Test
    fun `speak with DIVINATION_REPORT prompts for target among multiple divinations and auto-fills the true result`() {
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val io = CapturingIO(StatementType.DIVINATION_REPORT.displayName, "Wolf")
        val human = HumanPlayer(Role.SEER, "Human", io)
        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, human)
        GameEvent.Divined.send(villager, DivineResult.NOT_WEREWOLF, human)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human, wolf, villager))

        val statement = human.discuss(context)

        assertEquals(Statement.DivinationReport(human, wolf, DivineResult.WEREWOLF), statement)
        assertEquals(2, io.promptedChoices.size)
    }

    @Test
    fun `speak with MEDIUM_REPORT prompts for target among multiple mediums and auto-fills the true result`() {
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val io = CapturingIO(StatementType.MEDIUM_REPORT.displayName, "Wolf")
        val human = HumanPlayer(Role.MEDIUM, "Human", io)
        GameEvent.MediumRevealed.send(wolf, MediumResult.WEREWOLF, human)
        GameEvent.MediumRevealed.send(villager, MediumResult.NOT_WEREWOLF, human)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human, wolf, villager))

        val statement = human.discuss(context)

        assertEquals(Statement.MediumReport(human, wolf, MediumResult.WEREWOLF), statement)
        assertEquals(2, io.promptedChoices.size)
    }

    @Test
    fun `speak with DIVINATION_REPORT attaches the entered comment`() {
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val io = CapturingIO(
            StatementType.DIVINATION_REPORT.displayName,
            freeTextAnswer = "昨日の議論で怪しいと思っていました",
        )
        val human = HumanPlayer(Role.SEER, "Human", io)
        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, human)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human, wolf))

        val statement = human.discuss(context)

        assertEquals(
            Statement.DivinationReport(human, wolf, DivineResult.WEREWOLF, "昨日の議論で怪しいと思っていました"),
            statement,
        )
    }

    @Test
    fun `speak with MEDIUM_REPORT attaches the entered comment`() {
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val io = CapturingIO(
            StatementType.MEDIUM_REPORT.displayName,
            freeTextAnswer = "無実の方を処刑してしまい申し訳ない気持ちです",
        )
        val human = HumanPlayer(Role.MEDIUM, "Human", io)
        GameEvent.MediumRevealed.send(villager, MediumResult.NOT_WEREWOLF, human)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human, villager))

        val statement = human.discuss(context)

        assertEquals(
            Statement.MediumReport(human, villager, MediumResult.NOT_WEREWOLF, "無実の方を処刑してしまい申し訳ない気持ちです"),
            statement,
        )
    }

    @Test
    fun `speak with DIVINATION_REPORT as werewolf allows freely choosing any target and result`() {
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val io = CapturingIO(
            StatementType.DIVINATION_REPORT.displayName,
            "V1",
            DivineResult.WEREWOLF.displayName,
        )
        val wolf = HumanPlayer(Role.WEREWOLF, "Wolf", io)
        val allPlayers = AllPlayers(
            TestLodge(wolf to Role.WEREWOLF, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER).create().playerManager
        )
        GameEvent.PlayersAnnounced.send(listOf(wolf, villager1, villager2), allPlayers)
        val context = DiscussionContext.Open(1, 1, listOf(wolf), listOf(wolf, villager1, villager2))

        val statement = wolf.discuss(context)

        assertEquals(Statement.DivinationReport(wolf, villager1, DivineResult.WEREWOLF), statement)
        assertEquals(3, io.promptedChoices.size)
    }

    @Test
    fun `speak with ROLE_CLAIM prompts for role`() {
        val io = CapturingIO(StatementType.ROLE_CLAIM.displayName, Role.SEER.displayName)
        val human = HumanPlayer(Role.MADMAN, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.RoleClaim(human, Role.SEER), statement)
        assertEquals(2, io.promptedChoices.size)
    }

    @Test
    fun `villager has only PLAIN available and is not prompted for type`() {
        val io = CapturingIO(freeTextAnswer = "hello")
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.Plain(human, "hello"), statement)
        assertTrue(io.promptedChoices.isEmpty())
    }

    @Test
    fun `speak with ROLE_CLAIM skips role prompt when only one role is claimable`() {
        val io = CapturingIO(StatementType.ROLE_CLAIM.displayName)
        val human = HumanPlayer(Role.SEER, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.RoleClaim(human, Role.SEER), statement)
        assertEquals(1, io.promptedChoices.size)
    }

    @Test
    fun `seer who already claimed and has nothing new to report is not prompted for type`() {
        val io = CapturingIO(freeTextAnswer = "hello")
        val human = HumanPlayer(Role.SEER, "Human", io)
        val allPlayers = AllPlayers(TestLodge(human to Role.SEER).create().playerManager)
        GameEvent.StatementMade.send(1, human.name, Statement.RoleClaim(human, Role.SEER), allPlayers)
        val context = DiscussionContext.Open(2, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.Plain(human, "hello"), statement)
        assertTrue(io.promptedChoices.isEmpty())
    }

    @Test
    fun `speak with ROLE_CLAIM attaches the entered comment`() {
        val io = CapturingIO(
            StatementType.ROLE_CLAIM.displayName,
            Role.SEER.displayName,
            freeTextAnswer = "信じてください",
        )
        val human = HumanPlayer(Role.MADMAN, "Human", io)
        val context = DiscussionContext.Open(1, 1, listOf(human), listOf(human))

        val statement = human.discuss(context)

        assertEquals(Statement.RoleClaim(human, Role.SEER, "信じてください"), statement)
    }

    @Test
    fun `TimeChanged to Morning updates atmosphere to MORNING`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val allPlayers = AllPlayers(TestLodge(human to Role.VILLAGER).create().playerManager)

        GameEvent.TimeChanged.send(TimeOfDay.Morning, allPlayers)

        assertEquals(listOf(Atmosphere.MORNING), io.capturedAtmospheres)
    }

    @Test
    fun `TimeChanged to Night updates atmosphere to NIGHT`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val allPlayers = AllPlayers(TestLodge(human to Role.VILLAGER).create().playerManager)

        GameEvent.TimeChanged.send(TimeOfDay.Night(1), allPlayers)

        assertEquals(listOf(Atmosphere.NIGHT), io.capturedAtmospheres)
    }

    @Test
    fun `DiscussionStarted updates atmosphere to DAY`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val allPlayers = AllPlayers(TestLodge(human to Role.VILLAGER).create().playerManager)

        GameEvent.DiscussionStarted.send(1, allPlayers)

        assertEquals(listOf(Atmosphere.DAY), io.capturedAtmospheres)
    }

    @Test
    fun `VoteStarted updates atmosphere to VOTE`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val allPlayers = AllPlayers(TestLodge(human to Role.VILLAGER).create().playerManager)

        GameEvent.VoteStarted.send(1, allPlayers)

        assertEquals(listOf(Atmosphere.VOTE), io.capturedAtmospheres)
    }

    @Test
    fun `ConclaveStarted does not update atmosphere since it is part of night`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)
        val wolf2 = ReceivingPlayer(Role.WEREWOLF, "Wolf2")
        val setup = TestLodge(human to Role.WEREWOLF, wolf2 to Role.WEREWOLF).create()

        GameEvent.ConclaveStarted.send(1, Wolves(setup.oracle, setup.playerManager))

        assertTrue(io.capturedAtmospheres.isEmpty())
    }

    @Test
    fun `GameResult with citizen win updates epilogue mood to CITIZEN_WIN`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)

        GameEvent.GameResult.send(Side.CITIZEN, true, human)

        assertEquals(listOf(EpilogueMood.CITIZEN_WIN), io.capturedEpilogueMoods)
    }

    @Test
    fun `GameResult with citizen win and werewolf player updates epilogue mood to CITIZEN_LOSE`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)

        GameEvent.GameResult.send(Side.CITIZEN, false, human)

        assertEquals(listOf(EpilogueMood.CITIZEN_LOSE), io.capturedEpilogueMoods)
    }

    @Test
    fun `GameResult with werewolf win updates epilogue mood to WEREWOLF_WIN`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)

        GameEvent.GameResult.send(Side.WEREWOLF, true, human)

        assertEquals(listOf(EpilogueMood.WEREWOLF_WIN), io.capturedEpilogueMoods)
    }

    @Test
    fun `GameResult with werewolf win and villager player updates epilogue mood to WEREWOLF_LOSE`() {
        val io = CapturingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)

        GameEvent.GameResult.send(Side.WEREWOLF, false, human)

        assertEquals(listOf(EpilogueMood.WEREWOLF_LOSE), io.capturedEpilogueMoods)
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
