package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class OracleWerewolvesTest {

    @Test
    fun `returns only werewolf players from mixed team`() {
        val wolf1 = NothingPlayer(Role.WEREWOLF, "Wolf1")
        val wolf2 = NothingPlayer(Role.WEREWOLF, "Wolf2")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val oracle = Oracle(mapOf(wolf1 to Role.WEREWOLF, wolf2 to Role.WEREWOLF, villager to Role.VILLAGER))

        assertEquals(listOf(wolf1, wolf2), oracle.werewolves(listOf(wolf1, wolf2, villager)))
    }

    @Test
    fun `returns empty list when no werewolves are alive`() {
        val villager1 = NothingPlayer(Role.VILLAGER, "V1")
        val villager2 = NothingPlayer(Role.VILLAGER, "V2")
        val oracle = Oracle(mapOf(villager1 to Role.VILLAGER, villager2 to Role.VILLAGER))

        assertEquals(emptyList(), oracle.werewolves(listOf(villager1, villager2)))
    }
}