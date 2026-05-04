package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EndPhaseTest {

    private class WatchingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        var gameOver: GameEvent.GameOver? = null
        var gameResult: GameEvent.GameResult? = null
        var epilogueEvents: List<GameEvent>? = null

        override fun onReceive(event: GameEvent) {
            when (event) {
                is GameEvent.GameOver -> gameOver = event
                is GameEvent.GameResult -> gameResult = event
                else -> {}
            }
        }

        override fun watchEpilogue(events: List<GameEvent>) {
            epilogueEvents = events
        }
    }

    private fun fakeCitizenWinSignal(): GameOverSignal = try {
        GameOverSignal.throwIfGameOver(AliveCounts(mapOf(Side.CITIZEN to 2, Side.WEREWOLF to 0)))
        error("unreachable")
    } catch (s: GameOverSignal) { s }

    @Test
    fun `GameOver is broadcast with winning side`() {
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(villager to Role.VILLAGER).create()

        EndPhase(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).proceed()

        assertEquals(Side.CITIZEN, villager.gameOver?.winnerSide)
    }

    @Test
    fun `winner and loser receive correct GameResult`() {
        val wolf = WatchingPlayer(Role.WEREWOLF, "Wolf")
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()

        EndPhase(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).proceed()

        assertEquals(false, wolf.gameResult?.isWinner)
        assertEquals(true, villager.gameResult?.isWinner)
    }

    @Test
    fun `watchEpilogue is called for all players`() {
        val wolf = WatchingPlayer(Role.WEREWOLF, "Wolf")
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()

        EndPhase(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).proceed()

        assertNotNull(wolf.epilogueEvents)
        assertNotNull(villager.epilogueEvents)
    }
}

