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
import werewolf.view.PlayerStatusView
import werewolf.view.ReportEntry

class GameNoteTest {

    private class CapturingIO : HumanIO {
        val panels = mutableListOf<PlayerStatusView>()
        val divinationPanels = mutableListOf<DivinationView>()
        override fun display(view: RecallView) {}
        override fun updatePlayerStatusPanel(view: PlayerStatusView) { panels += view }
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

    private fun statusOf(view: PlayerStatusView, name: String): PlayerStatus = view.players.single { it.name == name }.status

    private fun claimedRoleOf(view: PlayerStatusView, name: String): String? = view.players.single { it.name == name }.claimedRole

    @Test
    fun `all players appear as alive at game start`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)

        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        val summary = io.panels.last()
        assertTrue(summary.players.all { it.status == PlayerStatus.ALIVE })
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

        assertEquals(PlayerStatus.EXECUTED, statusOf(io.panels.last(), "V2"))
        assertEquals(PlayerStatus.ALIVE, statusOf(io.panels.last(), "V1"))
    }

    @Test
    fun `player is marked as attacked after night kill`() {
        val io = CapturingIO()
        // V2 + V3 to avoid game-over when V2 is attacked (wolf would equal citizens)
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "V3" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }

        gameSetup.playerManager.kill(v2)

        assertEquals(PlayerStatus.ATTACKED, statusOf(io.panels.last(), "V2"))
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
    fun `divined result appears in divine results of divination panel`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val human = gameSetup.playerManager.allPlayers.single { it.name == "V1" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }

        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, human)

        assertEquals(mapOf("Wolf" to true), io.divinationPanels.last().divineResults)
    }

    @Test
    fun `medium result appears in medium results of divination panel`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val human = gameSetup.playerManager.allPlayers.single { it.name == "V1" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }

        GameEvent.MediumRevealed.send(wolf, MediumResult.WEREWOLF, human)

        assertEquals(mapOf("Wolf" to true), io.divinationPanels.last().mediumResults)
    }

    @Test
    fun `claimed divine result appears in divine reports of divination panel`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(1, "V2", Statement.DivinationReport(v2, wolf, DivineResult.WEREWOLF), allPlayers)

        assertEquals(
            listOf(ReportEntry("V2", "Wolf", isWerewolf = true)),
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

    @Test
    fun `ROLE_CLAIM statement appears as the player's claimed role`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(1, "V2", Statement.RoleClaim(v2, Role.SEER), allPlayers)

        assertEquals(Role.SEER.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `DivinationReport is treated as a seer claim`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(1, "V2", Statement.DivinationReport(v2, wolf, DivineResult.WEREWOLF), allPlayers)

        assertEquals(Role.SEER.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `MediumReport is treated as a medium claim`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(1, "V2", Statement.MediumReport(v2, wolf, MediumResult.WEREWOLF), allPlayers)

        assertEquals(Role.MEDIUM.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `later claim overrides an earlier one for the same player`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(1, "V2", Statement.RoleClaim(v2, Role.SEER), allPlayers)
        GameEvent.StatementMade.send(1, "V2", Statement.RoleClaim(v2, Role.VILLAGER), allPlayers)

        assertEquals(Role.VILLAGER.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `mentioning a role in a PLAIN statement is not treated as a claim`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(1, "V2", Statement.Plain(v2, "私は${Role.SEER.displayName}です"), allPlayers)

        assertEquals(null, claimedRoleOf(io.panels.last(), "V2"))
    }
}
