package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameRecapTest {

    private fun playerManager(vararg players: Player): PlayerManager {
        val oracle = Oracle(players.associateWith { Role.VILLAGER })
        return PlayerManager(players.toList(), oracle)
    }

    private fun anySignal(): GameOverSignal {
        return try {
            GameOverSignal.throwIfGameOver(AliveCounts(mapOf(Side.CITIZEN to 0, Side.WEREWOLF to 1)))
            error("unreachable")
        } catch (s: GameOverSignal) { s }
    }

    @Test
    fun `public events appear only once even when received by multiple players`() {
        val playerA = ReceivingPlayer(Role.VILLAGER, "A")
        val playerB = ReceivingPlayer(Role.VILLAGER, "B")
        val pm = playerManager(playerA, playerB)

        GameEvent.TimeChanged.send(TimeOfDay.Night(1), pm.allPlayers)

        val events = GameRecap(pm, anySignal()).events()
        assertEquals(1, events.size)
    }

    @Test
    fun `private events from each player are all collected`() {
        val playerA = ReceivingPlayer(Role.VILLAGER, "A")
        val playerB = ReceivingPlayer(Role.VILLAGER, "B")
        val pm = playerManager(playerA, playerB)

        GameEvent.RoleAssigned.send(Role.VILLAGER, playerA)
        GameEvent.RoleAssigned.send(Role.VILLAGER, playerB)

        val events = GameRecap(pm, anySignal()).events()
        assertEquals(2, events.size)
    }

    @Test
    fun `events are sorted by creation order across players`() {
        val playerA = ReceivingPlayer(Role.VILLAGER, "A")
        val playerB = ReceivingPlayer(Role.VILLAGER, "B")
        val pm = playerManager(playerA, playerB)

        // 作成順: RoleAssigned(A) → RoleAssigned(B) → TimeChanged(全員)
        // flatMap後: [RoleAssigned(A), TimeChanged, RoleAssigned(B), TimeChanged]
        // distinct後: [RoleAssigned(A), TimeChanged, RoleAssigned(B)] ← ソートなしでは誤順
        // sortedBy後: [RoleAssigned(A), RoleAssigned(B), TimeChanged] ← 正しい順
        GameEvent.RoleAssigned.send(Role.VILLAGER, playerA)
        GameEvent.RoleAssigned.send(Role.VILLAGER, playerB)
        GameEvent.TimeChanged.send(TimeOfDay.Night(1), pm.allPlayers)

        val events = GameRecap(pm, anySignal()).events()
        assertEquals(3, events.size)
        assertEquals("A", events[0].recipientName)
        assertEquals("B", events[1].recipientName)
        assertTrue(events[2] is GameEvent.TimeChanged)
    }
}
