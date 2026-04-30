package org.example

import kotlin.test.Test
import kotlin.test.assertFalse

class OracleFirstNightDivineTest {

    private class DivinationTrackingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        val divinedTargets = mutableListOf<Player>()
        override fun onReceive(event: GameEvent) {
            when (event) {
                is GameEvent.Divined -> divinedTargets.add(event.target)
                else -> error("unexpected event in oracle first night divine test: $event")
            }
        }
    }

    @Test
    fun `never selects self as divine target`() {
        val seer = DivinationTrackingPlayer(Role.SEER, "Seer")
        val villager1 = NothingPlayer(Role.VILLAGER, "V1")
        val villager2 = NothingPlayer(Role.VILLAGER, "V2")
        val oracle = Oracle(mapOf(seer to Role.SEER, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER))

        repeat(100) { oracle.firstNightDivine(seer, listOf(seer, villager1, villager2)) }

        assertFalse(seer in seer.divinedTargets)
    }

    @Test
    fun `never selects werewolf as divine target`() {
        val seer = DivinationTrackingPlayer(Role.SEER, "Seer")
        val werewolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val oracle = Oracle(mapOf(seer to Role.SEER, werewolf to Role.WEREWOLF, villager to Role.VILLAGER))

        repeat(100) { oracle.firstNightDivine(seer, listOf(seer, werewolf, villager)) }

        assertFalse(werewolf in seer.divinedTargets)
    }
}