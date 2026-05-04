package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NightPhaseTest {

    private class FixedTargetPlayer(
        role: Role, name: String, private val target: Player
    ) : ReceivingPlayer(role, name) {
        override fun selectTarget(context: SelectionContext): Player {
            require(target in context.candidates()) { "fixed target '${target.name}' is not in candidates" }
            return target
        }
    }

    @Test
    fun `wolf attack kills target and sets nightDeath`() {
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val villager3 = ReceivingPlayer(Role.VILLAGER, "V3")
        val wolf = FixedTargetPlayer(Role.WEREWOLF, "Wolf", villager1)
        val setup = TestLodge(
            wolf to Role.WEREWOLF,
            villager1 to Role.VILLAGER, villager2 to Role.VILLAGER, villager3 to Role.VILLAGER,
        ).create()

        val next = NightPhase(setup.playerManager, setup.oracle, 2).proceed()

        assertIs<MorningPhase>(next)
        assertEquals(villager1, setup.playerManager.nightDeath)
        assertFalse(setup.playerManager.players.contains(villager1))
        assertTrue(setup.playerManager.allPlayers.contains(villager1))
    }

    @Test
    fun `guard prevents wolf attack`() {
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        val extra = ReceivingPlayer(Role.VILLAGER, "Extra")
        val wolf = FixedTargetPlayer(Role.WEREWOLF, "Wolf", villager)
        val hunter = FixedTargetPlayer(Role.HUNTER, "Hunter", villager)
        val setup = TestLodge(
            wolf to Role.WEREWOLF, hunter to Role.HUNTER,
            villager to Role.VILLAGER, extra to Role.VILLAGER,
        ).create()

        NightPhase(setup.playerManager, setup.oracle, 2).proceed()

        assertNull(setup.playerManager.nightDeath)
        assertTrue(setup.playerManager.players.contains(villager))
    }

    @Test
    fun `wolf does not attack on first night`() {
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER).create()

        NightPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertNull(setup.playerManager.nightDeath)
    }
}
