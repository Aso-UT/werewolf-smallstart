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

    private fun setup(io: CapturingIO, vararg others: Pair<String, Role>): GameSetup {
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val otherPlayers = others.map { (name, role) -> SilentPlayer(role, name) to role }
        return TestLodge(*otherPlayers.toTypedArray(), human to Role.VILLAGER).create()
    }

    @Test
    fun `PlayersAnnounced triggers panel with all players as alive`() {
        val io = CapturingIO()
        val gameSetup = setup(io, "Alice" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.PlayersAnnounced.send(gameSetup.playerManager.allPlayers, allPlayers)

        val summary = io.panels.last()
        assertTrue(summary.players.values.all { it == PlayerStatus.ALIVE })
        assertEquals(3, summary.players.size)
    }

    @Test
    fun `PlayerExecuted marks player as executed`() {
        val io = CapturingIO()
        val gameSetup = setup(io, "Alice" to Role.VILLAGER)
        val allPlayers = AllPlayers(gameSetup.playerManager)
        val alice = gameSetup.playerManager.allPlayers.single { it.name == "Alice" }

        GameEvent.PlayersAnnounced.send(gameSetup.playerManager.allPlayers, allPlayers)
        GameEvent.PlayerExecuted.send(alice, allPlayers)

        assertEquals(PlayerStatus.EXECUTED, io.panels.last().players["Alice"])
        assertEquals(PlayerStatus.ALIVE, io.panels.last().players["Human"])
    }

    @Test
    fun `PlayerAttacked marks player as attacked`() {
        val io = CapturingIO()
        val gameSetup = setup(io, "Alice" to Role.VILLAGER)
        val allPlayers = AllPlayers(gameSetup.playerManager)
        val alice = gameSetup.playerManager.allPlayers.single { it.name == "Alice" }

        GameEvent.PlayersAnnounced.send(gameSetup.playerManager.allPlayers, allPlayers)
        GameEvent.PlayerAttacked.send(alice, allPlayers)

        assertEquals(PlayerStatus.ATTACKED, io.panels.last().players["Alice"])
    }

    @Test
    fun `irrelevant events do not trigger panel update`() {
        val io = CapturingIO()
        val gameSetup = setup(io, "Alice" to Role.VILLAGER)
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.PlayersAnnounced.send(gameSetup.playerManager.allPlayers, allPlayers)
        val countAfterAnnounce = io.panels.size

        GameEvent.DiscussionStarted.send(1, allPlayers)

        assertEquals(countAfterAnnounce, io.panels.size)
    }
}
