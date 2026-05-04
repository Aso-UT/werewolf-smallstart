package org.example

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OracleIsWinnerTest {

    private fun oracle(vararg pairs: Pair<Player, Role>) = Oracle(mapOf(*pairs))
    private fun player(role: Role) = NothingPlayer(role, role.name)

    @Test
    fun `villager wins when citizen side wins`() {
        val villager = player(Role.VILLAGER)
        assertTrue(oracle(villager to Role.VILLAGER).isWinner(villager, Side.CITIZEN))
    }

    @Test
    fun `villager loses when werewolf side wins`() {
        val villager = player(Role.VILLAGER)
        assertFalse(oracle(villager to Role.VILLAGER).isWinner(villager, Side.WEREWOLF))
    }

    @Test
    fun `werewolf wins when werewolf side wins`() {
        val wolf = player(Role.WEREWOLF)
        assertTrue(oracle(wolf to Role.WEREWOLF).isWinner(wolf, Side.WEREWOLF))
    }

    @Test
    fun `werewolf loses when citizen side wins`() {
        val wolf = player(Role.WEREWOLF)
        assertFalse(oracle(wolf to Role.WEREWOLF).isWinner(wolf, Side.CITIZEN))
    }

    @Test
    fun `madman wins when werewolf side wins`() {
        val madman = player(Role.MADMAN)
        assertTrue(oracle(madman to Role.MADMAN).isWinner(madman, Side.WEREWOLF))
    }

    @Test
    fun `madman loses when citizen side wins`() {
        val madman = player(Role.MADMAN)
        assertFalse(oracle(madman to Role.MADMAN).isWinner(madman, Side.CITIZEN))
    }
}
