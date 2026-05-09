package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EpilogueTest {

    private class WatchingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        var gameOver: GameEvent.GameOver? = null
        var gameResult: GameEvent.GameResult? = null
        var epilogueEvents: List<Recallable>? = null

        override fun onReceive(event: GameEvent) {
            when (event) {
                is GameEvent.GameOver -> gameOver = event
                is GameEvent.GameResult -> gameResult = event
                else -> {}
            }
        }

        override fun watchEpilogue(chronicles: List<Recallable>) {
            epilogueEvents = chronicles
        }
    }

    @Test
    fun `GameOver is broadcast with winning side`() {
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(villager to Role.VILLAGER).create()

        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        assertEquals(Side.CITIZEN, villager.gameOver?.winnerSide)
    }

    @Test
    fun `winner and loser receive correct GameResult`() {
        val wolf = WatchingPlayer(Role.WEREWOLF, "Wolf")
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()

        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        assertEquals(false, wolf.gameResult?.isWinner)
        assertEquals(true, villager.gameResult?.isWinner)
    }

    @Test
    fun `watchEpilogue is called for all players`() {
        val wolf = WatchingPlayer(Role.WEREWOLF, "Wolf")
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()

        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        assertNotNull(wolf.epilogueEvents)
        assertNotNull(villager.epilogueEvents)
    }
}
