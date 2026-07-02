package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import werewolf.game.AllPlayers
import werewolf.game.ChronicleView
import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.GameSetup
import werewolf.game.MediumResult
import werewolf.game.RecallView
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.human.HumanIO
import werewolf.human.HumanPlayer
import werewolf.phase.InitialPhase
import werewolf.view.ChoiceView
import werewolf.view.DivinationView
import werewolf.view.PlayerStatus
import werewolf.view.ReportEntry
import werewolf.view.SurvivalView

class GameNoteTest {

    private class CapturingIO : HumanIO {
        val panels = mutableListOf<SurvivalView>()
        val divinationPanels = mutableListOf<DivinationView>()
        override fun display(view: RecallView) {}
        override fun updatePanel(view: SurvivalView) { panels += view }
        override fun updateDivinationPanel(view: DivinationView) { divinationPanels += view }
        override fun promptChoice(view: ChoiceView): String = error("not expected")
        override fun promptFreeText(title: String, description: String): String = error("not expected")
        override fun watchEpilogue(chronicles: List<ChronicleView>) {}
    }

    private class SilentPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun onReceive(event: GameEvent) {}
    }

    private fun createGameWithHuman(io: CapturingIO, vararg others: Pair<String, Role>): GameSetup {
        val human = HumanPlayer(Role.VILLAGER, "V1", io)
        val otherPlayers = others.map { (name, role) -> SilentPlayer(role, name) to role }
        return TestLodge(*otherPlayers.toTypedArray(), human to Role.VILLAGER).create()
    }

    @Test
    fun `all players appear as alive at game start`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)

        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        val summary = io.panels.last()
        assertTrue(summary.players.values.all { it == PlayerStatus.ALIVE })
        assertEquals(3, summary.players.size)
    }

    @Test
    fun `player is marked as executed after execution`() {
        val io = CapturingIO()
        // V2 + V3 to avoid game-over when V2 is executed (wolf would equal citizens)
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "V3" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }

        gameSetup.playerManager.execute(v2)

        assertEquals(PlayerStatus.EXECUTED, io.panels.last().players["V2"])
        assertEquals(PlayerStatus.ALIVE, io.panels.last().players["V1"])
    }

    @Test
    fun `player is marked as attacked after night kill`() {
        val io = CapturingIO()
        // V2 + V3 to avoid game-over when V2 is attacked (wolf would equal citizens)
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "V3" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }

        gameSetup.playerManager.kill(v2)

        assertEquals(PlayerStatus.ATTACKED, io.panels.last().players["V2"])
    }

    @Test
    fun `panel is not updated for unrelated events`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val countAfterStart = io.panels.size

        GameEvent.DiscussionStarted.send(1, AllPlayers(gameSetup.playerManager))

        assertEquals(countAfterStart, io.panels.size)
    }

    @Test
    fun `divined result appears as trusted in divination panel`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val human = gameSetup.playerManager.allPlayers.single { it.name == "V1" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }

        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, human)

        assertEquals(
            listOf(ReportEntry("V1", "Wolf", "人狼", trusted = true)),
            io.divinationPanels.last().divineReports,
        )
    }

    @Test
    fun `medium result appears as trusted in divination panel`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val human = gameSetup.playerManager.allPlayers.single { it.name == "V1" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }

        GameEvent.MediumRevealed.send(wolf, MediumResult.WEREWOLF, human)

        assertEquals(
            listOf(ReportEntry("V1", "Wolf", "人狼である", trusted = true)),
            io.divinationPanels.last().mediumReports,
        )
    }

    @Test
    fun `claimed divine result appears as untrusted in divination panel`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(1, "V2", Statement.DivinationReport(v2, wolf, DivineResult.WEREWOLF), allPlayers)

        assertEquals(
            listOf(ReportEntry("V2", "Wolf", "人狼", trusted = false)),
            io.divinationPanels.last().divineReports,
        )
    }

    @Test
    fun `divination panel is not updated for unrelated events`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val countAfterStart = io.divinationPanels.size

        GameEvent.DiscussionStarted.send(1, AllPlayers(gameSetup.playerManager))

        assertEquals(countAfterStart, io.divinationPanels.size)
    }
}
