package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Role
import werewolf.human.HumanPlayer

class HumanPlayerSpeakingUrgeTest {

    private class UrgeAnsweringIO(private val wantsToSpeak: Boolean) : NothingHumanIO() {
        override fun promptSpeakingUrge(title: String, seconds: Int): Boolean = wantsToSpeak
    }

    private fun eagernessOf(wantsToSpeak: Boolean): Eagerness {
        val player = HumanPlayer(Role.VILLAGER, "Human", UrgeAnsweringIO(wantsToSpeak))
        val context = DiscussionContext.Open(1, 1, listOf(player), listOf(player))
        return player.eagerness(context)
    }

    @Test
    fun `wanting to speak gives higher eagerness than the default`() {
        assertTrue(eagernessOf(wantsToSpeak = true) > Eagerness.DEFAULT)
    }

    @Test
    fun `not wanting to speak gives lower eagerness than the default`() {
        assertTrue(eagernessOf(wantsToSpeak = false) < Eagerness.DEFAULT)
    }

    private class CallRecordingIO : NothingHumanIO() {
        val calls = mutableListOf<String>()
        override fun waitUntilReady() { calls.add("waitUntilReady") }
        override fun promptSpeakingUrge(title: String, seconds: Int): Boolean = false.also { calls.add("promptSpeakingUrge") }
    }

    @Test
    fun `waits until ready before asking so that the wait is not counted as response time`() {
        val io = CallRecordingIO()
        val player = HumanPlayer(Role.VILLAGER, "Human", io)

        player.eagerness(DiscussionContext.Open(1, 1, listOf(player), listOf(player)))

        assertEquals(listOf("waitUntilReady", "promptSpeakingUrge"), io.calls)
    }
}
