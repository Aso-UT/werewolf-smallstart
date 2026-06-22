package werewolf

import werewolf.game.GameOverSignal
import werewolf.game.RecallView
import werewolf.game.Role
import werewolf.human.PlayerIO
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlayerIOTest {

    private class TestPlayerIO(
        private val choiceInputs: ArrayDeque<String> = ArrayDeque(),
        private val playerInputs: ArrayDeque<String> = ArrayDeque(),
        private val freeTextInputs: ArrayDeque<String> = ArrayDeque(),
    ) : PlayerIO() {
        val messages = mutableListOf<Pair<String, String>>()
        override fun display(view: RecallView) = error("display not expected")
        override fun sendMessage(title: String, content: String) { messages += title to content }
        override fun readFreeText(): String = freeTextInputs.removeFirst()
        override fun readChoice(): String = choiceInputs.removeFirst()
        override fun readPlayer(): String = playerInputs.removeFirst()
    }

    private fun choiceIO(vararg inputs: String) =
        TestPlayerIO(choiceInputs = ArrayDeque(inputs.toList()))

    private fun playerIO(vararg inputs: String) =
        TestPlayerIO(playerInputs = ArrayDeque(inputs.toList()))

    @Test
    fun `promptFreeText sends message and returns input`() {
        val io = TestPlayerIO(freeTextInputs = ArrayDeque(listOf("hello")))
        val result = io.promptFreeText("title", "content")
        assertEquals("hello", result)
        assertEquals("title" to "content", io.messages.single())
    }

    @Test
    fun `promptChoice returns zero-based index for valid input`() {
        val io = choiceIO("2")
        assertEquals(1, io.promptChoice("title", "content", listOf("A", "B", "C")))
    }

    @Test
    fun `promptChoice retries on out-of-range input then succeeds`() {
        val io = choiceIO("0", "4", "2")
        assertEquals(1, io.promptChoice("title", "content", listOf("A", "B", "C")))
    }

    @Test
    fun `promptChoice retries on non-numeric input then succeeds`() {
        val io = choiceIO("abc", "1")
        assertEquals(0, io.promptChoice("title", "content", listOf("A")))
    }

    @Test
    fun `promptChoice throws GameOverSignal on abort password`() {
        val io = choiceIO("4423")
        assertFailsWith<GameOverSignal> {
            io.promptChoice("title", "content", listOf("A"))
        }
    }

    @Test
    fun `promptPlayer returns player matching name`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val bob = NothingPlayer(Role.VILLAGER, "Bob")
        val io = playerIO("Alice")
        assertEquals(alice, io.promptPlayer("title", "content", listOf(alice, bob)))
    }

    @Test
    fun `promptPlayer retries on unknown name then succeeds`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val io = playerIO("Charlie", "Alice")
        assertEquals(alice, io.promptPlayer("title", "content", listOf(alice)))
    }
}
