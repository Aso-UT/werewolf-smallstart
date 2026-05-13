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
    ) : RecordingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = Choice(this, context, target, "固定ターゲット")
    }

    private open class RecordingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        val received = mutableListOf<GameEvent>()
        override fun onReceive(event: GameEvent) { received.add(event) }
    }

    private class ConclavingWolf(name: String) : ReceivingPlayer(Role.WEREWOLF, name) {
        val heardStatements = mutableListOf<GameEvent.WerewolfStatementMade>()
        override fun speak(context: DiscussionContext): Claim = Claim(this, context, Statement.Plain(""), "")
        override fun onReceive(event: GameEvent) {
            if (event is GameEvent.WerewolfStatementMade) heardStatements.add(event)
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

    @Test
    fun `wolves conduct conclave when multiple wolves are present`() {
        val wolf1 = ConclavingWolf("Wolf1")
        val wolf2 = ConclavingWolf("Wolf2")
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val setup = TestLodge(
            wolf1 to Role.WEREWOLF, wolf2 to Role.WEREWOLF,
            villager1 to Role.VILLAGER, villager2 to Role.VILLAGER,
        ).create()

        NightPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertTrue(wolf1.heardStatements.isNotEmpty())
    }

    @Test
    fun `seer receives divination result for chosen player`() {
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val villager3 = ReceivingPlayer(Role.VILLAGER, "V3")
        val wolf = FixedTargetPlayer(Role.WEREWOLF, "Wolf", villager1)
        val seer = FixedTargetPlayer(Role.SEER, "Seer", villager1)
        val setup = TestLodge(
            wolf to Role.WEREWOLF, seer to Role.SEER,
            villager1 to Role.VILLAGER, villager2 to Role.VILLAGER, villager3 to Role.VILLAGER,
        ).create()

        NightPhase(setup.playerManager, setup.oracle, 2).proceed()

        val divined = seer.received.filterIsInstance<GameEvent.Divined>()
        assertEquals(1, divined.size)
        assertEquals(villager1, divined.single().target)
    }

    @Test
    fun `medium receives medium result for executed player`() {
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val villager3 = ReceivingPlayer(Role.VILLAGER, "V3")
        val wolf = FixedTargetPlayer(Role.WEREWOLF, "Wolf", villager2)
        val medium = RecordingPlayer(Role.MEDIUM, "Medium")
        val setup = TestLodge(
            wolf to Role.WEREWOLF, medium to Role.MEDIUM,
            villager1 to Role.VILLAGER, villager2 to Role.VILLAGER, villager3 to Role.VILLAGER,
        ).create()
        setup.playerManager.execute(villager1)

        NightPhase(setup.playerManager, setup.oracle, 2).proceed()

        val revealed = medium.received.filterIsInstance<GameEvent.MediumRevealed>()
        assertEquals(1, revealed.size)
        assertEquals(villager1, revealed.single().target)
    }
}

