package werewolf

import werewolf.game.*
import werewolf.phase.*
import werewolf.cpu.*
import werewolf.ai.*
import werewolf.human.*
import werewolf.lodge.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionContextAttackTest {

    @Test
    fun `excludes self from candidates`() {
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val players = listOf(wolf, villager)

        val candidates = SelectionContext.Attack(wolf, players, emptyList()).candidates()

        assertFalse(wolf in candidates)
        assertTrue(villager in candidates)
    }

    @Test
    fun `excludes werewolf allies from candidates`() {
        val wolf1 = NothingPlayer(Role.WEREWOLF, "Wolf1")
        val wolf2 = NothingPlayer(Role.WEREWOLF, "Wolf2")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val players = listOf(wolf1, wolf2, villager)

        val candidates = SelectionContext.Attack(wolf1, players, allies = listOf(wolf2)).candidates()

        assertEquals(listOf(villager), candidates)
    }

    @Test
    fun `includes all non-self players when no allies`() {
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager1 = NothingPlayer(Role.VILLAGER, "V1")
        val villager2 = NothingPlayer(Role.VILLAGER, "V2")
        val players = listOf(wolf, villager1, villager2)

        val candidates = SelectionContext.Attack(wolf, players, emptyList()).candidates()

        assertEquals(listOf(villager1, villager2), candidates)
    }
}
