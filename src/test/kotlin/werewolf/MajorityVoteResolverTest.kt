package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertFailsWith

class MajorityVoteResolverTest {

    @Test
    fun `returns null for empty votes`() {
        assertNull(MajorityVoteResolver.resolve(emptyList<String>()))
    }

    @Test
    fun `returns the only candidate when all votes are the same`() {
        assertEquals("A", MajorityVoteResolver.resolve(listOf("A", "A", "A")))
    }

    @Test
    fun `returns the majority candidate`() {
        assertEquals("B", MajorityVoteResolver.resolve(listOf("A", "B", "B")))
    }

    @Test
    fun `returns one of the tied candidates when votes are split evenly`() {
        val tied = setOf("A", "B")
        val results = (1..100).map { MajorityVoteResolver.resolve(listOf("A", "B")) }.toSet()
        assertEquals(tied, results)
    }

    @Test
    fun `returns one of the top tied candidates ignoring lower-voted ones`() {
        val tied = setOf("A", "B")
        val results = (1..100).map { MajorityVoteResolver.resolve(listOf("A", "A", "B", "B", "C")) }.toSet()
        assertEquals(tied, results)
    }

    @Test
    fun `resolveNonEmpty throws for empty votes`() {
        assertFailsWith<IllegalArgumentException> {
            MajorityVoteResolver.resolveNonEmpty(emptyList<String>())
        }
    }

    @Test
    fun `resolveNonEmpty returns the majority candidate`() {
        assertEquals("B", MajorityVoteResolver.resolveNonEmpty(listOf("A", "B", "B")))
    }
}
