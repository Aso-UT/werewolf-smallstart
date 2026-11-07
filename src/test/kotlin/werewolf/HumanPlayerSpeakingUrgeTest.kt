package werewolf

import kotlin.test.Test
import kotlin.test.assertTrue
import werewolf.game.DiscussionContext
import werewolf.game.Role
import werewolf.human.HumanPlayer

class HumanPlayerSpeakingUrgeTest {

    private class UrgeAnsweringIO(private val wantsToSpeak: Boolean) : NothingHumanIO() {
        override fun promptSpeakingUrge(title: String, seconds: Int): Boolean = wantsToSpeak
    }

    @Test
    fun `wanting to speak gives higher eagerness than not wanting to`() {
        val eager = HumanPlayer(Role.VILLAGER, "Eager", UrgeAnsweringIO(wantsToSpeak = true))
        val quiet = HumanPlayer(Role.VILLAGER, "Quiet", UrgeAnsweringIO(wantsToSpeak = false))
        val context = DiscussionContext.Open(1, 1, listOf(eager, quiet), listOf(eager, quiet))

        assertTrue(eager.eagerness(context) > quiet.eagerness(context))
    }
}
