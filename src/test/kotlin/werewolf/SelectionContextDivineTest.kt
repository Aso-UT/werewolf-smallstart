package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SelectionContextDivineTest {

    @Test
    fun `excludes self from candidates`() {
        val seer = NothingPlayer(Role.SEER, "Seer")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val players = listOf(seer, villager)

        val candidates = SelectionContext.Divine(seer, players, emptyList()).candidates()

        assertFalse(seer in candidates)
        assertTrue(villager in candidates)
    }

    @Test
    fun `excludes already divined players from candidates`() {
        val seer = NothingPlayer(Role.SEER, "Seer")
        val villager1 = NothingPlayer(Role.VILLAGER, "V1")
        val villager2 = NothingPlayer(Role.VILLAGER, "V2")
        val players = listOf(seer, villager1, villager2)

        val candidates = SelectionContext.Divine(seer, players, alreadyDivined = listOf(villager1)).candidates()

        assertEquals(listOf(villager2), candidates)
    }

    @Test
    fun `includes all non-self players when no one is divined yet`() {
        val seer = NothingPlayer(Role.SEER, "Seer")
        val villager1 = NothingPlayer(Role.VILLAGER, "V1")
        val villager2 = NothingPlayer(Role.VILLAGER, "V2")
        val players = listOf(seer, villager1, villager2)

        val candidates = SelectionContext.Divine(seer, players, emptyList()).candidates()

        assertEquals(listOf(villager1, villager2), candidates)
    }
}
