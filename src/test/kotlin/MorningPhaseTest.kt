package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class MorningPhaseTest {

    private class RecordingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        val morningReports = mutableListOf<GameEvent.MorningReport>()
        override fun onReceive(event: GameEvent) {
            if (event is GameEvent.MorningReport) morningReports.add(event)
        }
    }

    @Test
    fun `returns DayPhase`() {
        val setup = TestLodge(
            ReceivingPlayer(Role.WEREWOLF, "Wolf") to Role.WEREWOLF,
            ReceivingPlayer(Role.VILLAGER, "Villager") to Role.VILLAGER,
        ).create()

        val next = MorningPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertIs<DayPhase>(next)
    }

    @Test
    fun `MorningReport victim is null when no kill occurred`() {
        val observer = RecordingPlayer(Role.VILLAGER, "Observer")
        val setup = TestLodge(observer to Role.VILLAGER).create()

        MorningPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertNull(observer.morningReports.single().victim)
    }

    @Test
    fun `MorningReport victim is the killed player`() {
        val observer = RecordingPlayer(Role.VILLAGER, "Observer")
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val setup = TestLodge(
            ReceivingPlayer(Role.WEREWOLF, "Wolf") to Role.WEREWOLF,
            observer to Role.VILLAGER, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER,
        ).create()
        setup.playerManager.kill(villager1)

        MorningPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertEquals(villager1, observer.morningReports.single().victim)
    }

    @Test
    fun `nightDeath is cleared by bury as seen in consecutive MorningReports`() {
        val observer = RecordingPlayer(Role.VILLAGER, "Observer")
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val setup = TestLodge(
            ReceivingPlayer(Role.WEREWOLF, "Wolf") to Role.WEREWOLF,
            observer to Role.VILLAGER, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER,
        ).create()
        setup.playerManager.kill(villager1)

        MorningPhase(setup.playerManager, setup.oracle, 1).proceed()
        MorningPhase(setup.playerManager, setup.oracle, 2).proceed()

        assertEquals(villager1, observer.morningReports[0].victim)
        assertNull(observer.morningReports[1].victim)
    }
}
