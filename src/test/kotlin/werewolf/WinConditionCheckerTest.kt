package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class WinConditionCheckerTest {

    private fun counts(citizen: Int, werewolf: Int) =
        AliveCounts(mapOf(Side.CITIZEN to citizen, Side.WEREWOLF to werewolf))

    @Test
    fun `citizen wins when all werewolves are dead`() {
        assertEquals(Side.CITIZEN, WinConditionChecker.winningSide(counts(citizen = 3, werewolf = 0)))
    }

    @Test
    fun `werewolf wins when werewolves equal citizens`() {
        assertEquals(Side.WEREWOLF, WinConditionChecker.winningSide(counts(citizen = 2, werewolf = 2)))
    }

    @Test
    fun `werewolf wins when werewolves outnumber citizens`() {
        assertEquals(Side.WEREWOLF, WinConditionChecker.winningSide(counts(citizen = 1, werewolf = 2)))
    }

    @Test
    fun `game continues when citizens outnumber werewolves`() {
        assertNull(WinConditionChecker.winningSide(counts(citizen = 2, werewolf = 1)))
    }
}
