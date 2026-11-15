package werewolf

import werewolf.util.Cardinality
import werewolf.util.cardinality
import kotlin.test.Test
import kotlin.test.assertEquals

class CardinalityTest {
    @Test
    fun `an empty collection has none`() {
        assertEquals(Cardinality.None, emptyList<String>().cardinality())
    }

    @Test
    fun `a collection with one element has that one`() {
        assertEquals(Cardinality.One("a"), listOf("a").cardinality())
    }

    @Test
    fun `a collection with two or more elements has many in order`() {
        assertEquals(Cardinality.Many(listOf("a", "b")), listOf("a", "b").cardinality())
    }
}
