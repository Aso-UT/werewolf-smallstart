package werewolf

import kotlin.test.Test
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
}
