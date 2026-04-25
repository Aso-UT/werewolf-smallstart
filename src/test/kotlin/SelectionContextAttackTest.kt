package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionContextAttackTest {

    private class StubPlayer(override val name: String, role: Role) : Player(role) {
        override fun selectTarget(context: SelectionContext) = this
        override fun onReceive(event: GameEvent) = Unit
        override fun discuss(players: List<Player>) = ""
    }

    @Test
    fun `excludes self from candidates`() {
        val wolf = StubPlayer("Wolf", Role.WEREWOLF)
        val villager = StubPlayer("Villager", Role.VILLAGER)
        val players = listOf(wolf, villager)

        val candidates = SelectionContext.Attack(wolf, players).candidates()

        assertFalse(wolf in candidates)
        assertTrue(villager in candidates)
    }

    @Test
    fun `excludes werewolf allies from candidates`() {
        val wolf1 = StubPlayer("Wolf1", Role.WEREWOLF)
        val wolf2 = StubPlayer("Wolf2", Role.WEREWOLF)
        val villager = StubPlayer("Villager", Role.VILLAGER)
        val players = listOf(wolf1, wolf2, villager)

        val candidates = SelectionContext.Attack(wolf1, players, allies = listOf(wolf2)).candidates()

        assertEquals(listOf(villager), candidates)
    }

    @Test
    fun `includes all non-self players when no allies`() {
        val wolf = StubPlayer("Wolf", Role.WEREWOLF)
        val villager1 = StubPlayer("V1", Role.VILLAGER)
        val villager2 = StubPlayer("V2", Role.VILLAGER)
        val players = listOf(wolf, villager1, villager2)

        val candidates = SelectionContext.Attack(wolf, players).candidates()

        assertEquals(listOf(villager1, villager2), candidates)
    }
}
