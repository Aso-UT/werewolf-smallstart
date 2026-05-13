package werewolf

import werewolf.game.*
import werewolf.phase.*
import werewolf.cpu.*
import werewolf.ai.*
import werewolf.human.*
import werewolf.lodge.*

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameRecapTest {

    private fun playerManager(vararg players: Player): PlayerManager =
        TestLodge(*players.map { it to Role.VILLAGER }.toTypedArray()).create().playerManager

    private fun anySignal(): GameOverSignal {
        return try {
            GameOverSignal.throwIfGameOver(AliveCounts(mapOf(Side.CITIZEN to 0, Side.WEREWOLF to 1)))
            error("unreachable")
        } catch (s: GameOverSignal) { s }
    }

    @Test
    fun `public events appear only once even when received by multiple players`() {
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val pm = playerManager(v1, v2)

        GameEvent.TimeChanged.send(TimeOfDay.Night(1), AllPlayers(pm))

        val events = GameRecap(pm, anySignal()).events()
        assertEquals(1, events.size)
    }

    @Test
    fun `private events from each player are all collected`() {
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val pm = playerManager(v1, v2)

        GameEvent.RoleAssigned.send(Role.VILLAGER, v1)
        GameEvent.RoleAssigned.send(Role.VILLAGER, v2)

        val events = GameRecap(pm, anySignal()).events()
        assertEquals(2, events.size)
    }

    @Test
    fun `events are sorted by creation order across players`() {
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val pm = playerManager(v1, v2)

        // 作成順: RoleAssigned(A) → RoleAssigned(B) → TimeChanged(全員)
        // flatMap後: [RoleAssigned(A), TimeChanged, RoleAssigned(B), TimeChanged]
        // distinct後: [RoleAssigned(A), TimeChanged, RoleAssigned(B)] ← ソートなしでは誤順
        // sortedBy後: [RoleAssigned(A), RoleAssigned(B), TimeChanged] ← 正しい順
        GameEvent.RoleAssigned.send(Role.VILLAGER, v1)
        GameEvent.RoleAssigned.send(Role.VILLAGER, v2)
        GameEvent.TimeChanged.send(TimeOfDay.Night(1), AllPlayers(pm))

        val events = GameRecap(pm, anySignal()).events()
        assertEquals(3, events.size)
        assertContains(events[0].chronicle(), "V1")
        assertContains(events[1].chronicle(), "V2")
        assertTrue(events[2] is GameEvent.TimeChanged)
    }
}
