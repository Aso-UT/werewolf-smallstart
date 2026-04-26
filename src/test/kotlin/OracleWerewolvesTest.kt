package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class OracleWerewolvesTest {

    private class TestPlayer(role: Role, override val name: String) : Player(role) {
        override fun selectTarget(context: SelectionContext) = this
        override fun onReceive(event: GameEvent) {}
        override fun discuss(players: List<Player>): Statement = Statement.Plain("")
    }

    @Test
    fun `returns only werewolf players from mixed team`() {
        val wolf1 = TestPlayer(Role.WEREWOLF, "Wolf1")
        val wolf2 = TestPlayer(Role.WEREWOLF, "Wolf2")
        val villager = TestPlayer(Role.VILLAGER, "Villager")
        val oracle = Oracle(mapOf(wolf1 to Role.WEREWOLF, wolf2 to Role.WEREWOLF, villager to Role.VILLAGER))

        assertEquals(listOf(wolf1, wolf2), oracle.werewolves(listOf(wolf1, wolf2, villager)))
    }

    @Test
    fun `returns empty list when no werewolves are alive`() {
        val villager1 = TestPlayer(Role.VILLAGER, "V1")
        val villager2 = TestPlayer(Role.VILLAGER, "V2")
        val oracle = Oracle(mapOf(villager1 to Role.VILLAGER, villager2 to Role.VILLAGER))

        assertEquals(emptyList(), oracle.werewolves(listOf(villager1, villager2)))
    }
}