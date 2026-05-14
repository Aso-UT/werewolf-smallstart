package werewolf

import werewolf.game.*
import werewolf.phase.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EpilogueTest {

    private class ChoosingWatchingPlayer(role: Role, name: String) : WatchingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(this, context)
    }

    private open class WatchingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        var gameOver: GameEvent.GameOver? = null
        var gameResult: GameEvent.GameResult? = null
        val watchedEvents: MutableList<Recallable> = mutableListOf()

        override fun onReceive(event: GameEvent) {
            when (event) {
                is GameEvent.GameOver -> gameOver = event
                is GameEvent.GameResult -> gameResult = event
                else -> {}
            }
        }

        override fun watchEpilogue(chronicles: List<Recallable>) {
            watchedEvents.addAll(chronicles)
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

        assertTrue(wolf.watchedEvents.isNotEmpty())
        assertTrue(villager.watchedEvents.isNotEmpty())
    }

    @Test
    fun `choice made by player appears in epilogue chronicles`() {
        val wolf = WatchingPlayer(Role.WEREWOLF, "Wolf")
        val chooser = ChoosingWatchingPlayer(Role.VILLAGER, "Chooser")
        val setup = TestLodge(wolf to Role.WEREWOLF, chooser to Role.VILLAGER).create()
        val signal = fakeCitizenWinSignal()

        chooser.selectTarget(SelectionContext.Vote(chooser, setup.playerManager.players))

        Epilogue(setup.playerManager, setup.oracle, signal).perform()

        assertTrue(chooser.watchedEvents.any { it is Choice })
    }
}
