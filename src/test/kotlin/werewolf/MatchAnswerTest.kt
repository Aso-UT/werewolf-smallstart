package werewolf

import werewolf.ai.InvalidAiInputException
import werewolf.ai.matchAnswer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MatchAnswerTest {
    @Test
    fun `the one choice matching the answer is chosen`() {
        assertEquals("Bob", listOf("Alice", "Bob").matchAnswer("no match") { it == "Bob" })
    }

    @Test
    fun `an answer matching no choice is invalid input to be asked again`() {
        assertFailsWith<InvalidAiInputException> { listOf("Alice", "Bob").matchAnswer("no match") { it == "Charlie" } }
    }

    @Test
    fun `an answer matching several choices is a programming error, not invalid input`() {
        assertFailsWith<IllegalStateException> { listOf("Alice", "Alice").matchAnswer("no match") { it == "Alice" } }
    }
}
