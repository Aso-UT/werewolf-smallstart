package werewolf

import werewolf.game.Recallable
import werewolf.game.Role
import werewolf.human.HumanPlayer
import werewolf.human.PlayerIO

import kotlin.test.Test
import kotlin.test.assertEquals

class HumanPlayerTest {

    private class CapturingIO : PlayerIO() {
        val messages = mutableListOf<Pair<String, String>>()
        override fun sendMessage(title: String, content: String) { messages += title to content }
        override fun readFreeText(): String = error("not used")
        override fun readChoice(): String = error("not used")
        override fun readPlayer(): String = error("not used")
    }

    private fun recallable(chronicleText: String, intent: String? = null, redundant: Boolean = false) = object : Recallable() {
        override fun recall() = chronicleText
        override fun chronicle() = chronicleText
        override val intentForChronicle get() = intent
        override val isRedundantInChronicle get() = redundant
    }

    @Test
    fun `watchEpilogue shows chronicle when intentForChronicle is null`() {
        val io = CapturingIO()
        val player = HumanPlayer(Role.VILLAGER, "Player", io)
        player.watchEpilogue(listOf(recallable("some chronicle")))
        assertEquals("some chronicle", io.messages.last().second)
    }

    @Test
    fun `watchEpilogue appends intent on next line when intentForChronicle is present`() {
        val io = CapturingIO()
        val player = HumanPlayer(Role.VILLAGER, "Player", io)
        player.watchEpilogue(listOf(recallable("some chronicle", "some intent")))
        assertEquals("some chronicle\n  [some intent]", io.messages.last().second)
    }

    @Test
    fun `watchEpilogue excludes redundant records`() {
        val io = CapturingIO()
        val player = HumanPlayer(Role.VILLAGER, "Player", io)
        player.watchEpilogue(listOf(recallable("kept"), recallable("excluded", redundant = true)))
        assertEquals("kept", io.messages.last().second)
    }
}
