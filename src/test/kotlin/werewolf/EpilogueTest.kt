package werewolf

import werewolf.game.*
import werewolf.phase.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class EpilogueTest {

    private class ChoosingWatchingPlayer(role: Role, name: String) : WatchingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(this, context)
    }

    private open class WatchingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        var gameOver: GameEvent.GameOver? = null
        var gameResult: GameEvent.GameResult? = null
        val received: MutableList<GameEvent> = mutableListOf()
        val watchedEvents: MutableList<ChronicleView> = mutableListOf()

        override fun onReceive(event: GameEvent) {
            received += event
            when (event) {
                is GameEvent.GameOver -> gameOver = event
                is GameEvent.GameResult -> gameResult = event
                else -> {}
            }
        }

        override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) {
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
        assertEquals(Side.CITIZEN, wolf.gameResult?.winnerSide)
        assertEquals(Side.CITIZEN, villager.gameResult?.winnerSide)
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
    fun `GameOver and GameResult are not sent when game is aborted`() {
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(villager to Role.VILLAGER).create()

        Epilogue(setup.playerManager, setup.oracle, fakeAbortedSignal()).perform()

        assertNull(villager.gameOver)
        assertNull(villager.gameResult)
    }

    @Test
    fun `every player receives all players' roles`() {
        val wolf = WatchingPlayer(Role.WEREWOLF, "Wolf")
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()

        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        val expected = mapOf<Player, Role>(wolf to Role.WEREWOLF, villager to Role.VILLAGER)
        assertEquals(expected, wolf.received.filterIsInstance<GameEvent.RolesRevealed>().single().roles)
        assertEquals(expected, villager.received.filterIsInstance<GameEvent.RolesRevealed>().single().roles)
    }

    @Test
    fun `roles are revealed between GameOver and GameResult`() {
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(villager to Role.VILLAGER).create()

        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        assertEquals(
            listOf(GameEvent.GameOver::class, GameEvent.RolesRevealed::class, GameEvent.GameResult::class),
            villager.received.map { it::class },
        )
    }

    @Test
    fun `roles are revealed even when game is aborted`() {
        val villager = WatchingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(villager to Role.VILLAGER).create()

        Epilogue(setup.playerManager, setup.oracle, fakeAbortedSignal()).perform()

        assertEquals(mapOf<Player, Role>(villager to Role.VILLAGER), villager.received.filterIsInstance<GameEvent.RolesRevealed>().single().roles)
    }

    @Test
    fun `choice made by player appears in epilogue chronicles`() {
        val wolf = WatchingPlayer(Role.WEREWOLF, "Wolf")
        val chooser = ChoosingWatchingPlayer(Role.VILLAGER, "Chooser")
        val setup = TestLodge(wolf to Role.WEREWOLF, chooser to Role.VILLAGER).create()
        val signal = fakeCitizenWinSignal()

        chooser.selectTarget(SelectionContext.Vote(chooser, setup.playerManager.players))

        Epilogue(setup.playerManager, setup.oracle, signal).perform()

        assertTrue(chooser.watchedEvents.any { it is ChronicleView.Action })
    }
}
