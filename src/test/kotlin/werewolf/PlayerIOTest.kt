package werewolf

import werewolf.game.GameOverSignal
import werewolf.game.RecallView
import werewolf.human.PlayerIO
import werewolf.view.ChoiceView
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PlayerIOTest {

    private class TestPlayerIO(
        private val inputs: ArrayDeque<String> = ArrayDeque(),
    ) : PlayerIO() {
        val messages = mutableListOf<Pair<String, String>>()
        override fun display(view: RecallView) = error("display not expected")
        override fun sendMessage(title: String, content: String) { messages += title to content }
        override fun readInput(): String = inputs.removeFirst()
    }

    private fun inputIO(vararg inputs: String) = TestPlayerIO(ArrayDeque(inputs.toList()))

    @Test
    fun `promptFreeText sends message and returns input`() {
        val io = inputIO("hello")
        val result = io.promptFreeText("title", "content")
        assertEquals("hello", result)
        assertEquals("title" to "content", io.messages.single())
    }

    @Test
    fun `promptChoice returns option name for valid numeric input`() {
        val io = inputIO("2")
        assertEquals("B", io.promptChoice(ChoiceView("title", "content", listOf("A", "B", "C"))))
    }

    @Test
    fun `promptChoice retries on out-of-range input then succeeds`() {
        val io = inputIO("0", "4", "2")
        assertEquals("B", io.promptChoice(ChoiceView("title", "content", listOf("A", "B", "C"))))
    }

    @Test
    fun `promptChoice retries on non-numeric input then succeeds`() {
        val io = inputIO("abc", "1")
        assertEquals("A", io.promptChoice(ChoiceView("title", "content", listOf("A"))))
    }

    @Test
    fun `promptChoice throws GameOverSignal on abort password`() {
        val io = inputIO("4423")
        assertFailsWith<GameOverSignal> {
            io.promptChoice(ChoiceView("title", "content", listOf("A")))
        }
    }
}
