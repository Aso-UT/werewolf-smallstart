package org.example

import kotlin.test.Test
import kotlin.test.assertFalse

class OracleFirstNightDivineTest {

    private class RecordingPlayer(role: Role, override val name: String) : Player(role) {
        val divinedTargets = mutableListOf<Player>()
        override fun selectTarget(context: SelectionContext) = this
        override fun receive(event: GameEvent) { divinedTargets.add((event as GameEvent.Divined).target) }
        override fun discuss(players: List<Player>) = ""
    }

    @Test
    fun `never selects seer as divine target`() {
        val seer = RecordingPlayer(Role.SEER, "Seer")
        val villager1 = RecordingPlayer(Role.VILLAGER, "V1")
        val villager2 = RecordingPlayer(Role.VILLAGER, "V2")
        val oracle = Oracle(mapOf(seer to Role.SEER, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER))

        repeat(100) { oracle.firstNightDivine(seer, listOf(seer, villager1, villager2)) }

        assertFalse(seer in seer.divinedTargets)
    }

    @Test
    fun `never selects werewolf as divine target`() {
        val seer = RecordingPlayer(Role.SEER, "Seer")
        val werewolf = RecordingPlayer(Role.WEREWOLF, "Wolf")
        val villager = RecordingPlayer(Role.VILLAGER, "Villager")
        val oracle = Oracle(mapOf(seer to Role.SEER, werewolf to Role.WEREWOLF, villager to Role.VILLAGER))

        repeat(100) { oracle.firstNightDivine(seer, listOf(seer, werewolf, villager)) }

        assertFalse(werewolf in seer.divinedTargets)
    }
}
