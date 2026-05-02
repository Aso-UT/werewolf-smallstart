package org.example

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OracleIsWinnerTest {

    private fun oracle(vararg pairs: Pair<Player, Role>) = Oracle(mapOf(*pairs))
    private fun player(role: Role) = NothingPlayer(role, "p")

    @Test
    fun `villager wins when citizen side wins`() {
        val p = player(Role.VILLAGER)
        assertTrue(oracle(p to Role.VILLAGER).isWinner(p, Side.CITIZEN))
    }

    @Test
    fun `villager loses when werewolf side wins`() {
        val p = player(Role.VILLAGER)
        assertFalse(oracle(p to Role.VILLAGER).isWinner(p, Side.WEREWOLF))
    }

    @Test
    fun `werewolf wins when werewolf side wins`() {
        val p = player(Role.WEREWOLF)
        assertTrue(oracle(p to Role.WEREWOLF).isWinner(p, Side.WEREWOLF))
    }

    @Test
    fun `werewolf loses when citizen side wins`() {
        val p = player(Role.WEREWOLF)
        assertFalse(oracle(p to Role.WEREWOLF).isWinner(p, Side.CITIZEN))
    }

    @Test
    fun `madman wins when werewolf side wins`() {
        val p = player(Role.MADMAN)
        assertTrue(oracle(p to Role.MADMAN).isWinner(p, Side.WEREWOLF))
    }

    @Test
    fun `madman loses when citizen side wins`() {
        val p = player(Role.MADMAN)
        assertFalse(oracle(p to Role.MADMAN).isWinner(p, Side.CITIZEN))
    }
}
