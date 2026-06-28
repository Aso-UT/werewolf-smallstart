package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import werewolf.game.AllPlayers
import werewolf.game.ChronicleView
import werewolf.game.GameEvent
import werewolf.game.GameSetup
import werewolf.game.RecallView
import werewolf.game.Role
import werewolf.human.HumanIO
import werewolf.human.HumanPlayer
import werewolf.phase.InitialPhase
import werewolf.view.ChoiceView
import werewolf.view.PlayerStatus
import werewolf.view.SurvivalView

class GameNoteTest {

    private class CapturingIO : HumanIO {
        val panels = mutableListOf<SurvivalView>()
        override fun display(view: RecallView) {}
        override fun updatePanel(view: SurvivalView) { panels += view }
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
}
