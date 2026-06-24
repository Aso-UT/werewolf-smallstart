package werewolf

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.game.Role
import werewolf.human.HumanPlayer
import werewolf.human.PlayerIO
import werewolf.view.ChoiceView

import kotlin.test.Test
import kotlin.test.assertEquals

class HumanPlayerTest {

    private class CapturingIO : PlayerIO() {
        val messages = mutableListOf<Pair<String, String>>()
        override fun display(view: RecallView) = error("display not expected")
        override fun sendMessage(title: String, content: String) { messages += title to content }
        override fun readInput(): String = error("not used")
        override fun promptChoice(view: ChoiceView): String = error("not used")
    }

    @Test
    fun `watchEpilogue formats observation as bracket blocks`() {
        val io = CapturingIO()
        val player = HumanPlayer(Role.VILLAGER, "Player", io)
        player.watchEpilogue(listOf(ChronicleView.Observation("Player", "カテゴリ", "内容")))
        assertEquals("[Player] [カテゴリ] 内容", io.messages.last().second)
    }

    @Test
    fun `watchEpilogue formats action with intent on next line`() {
        val io = CapturingIO()
        val player = HumanPlayer(Role.VILLAGER, "Player", io)
        player.watchEpilogue(listOf(ChronicleView.Action("Player", "カテゴリ", "内容", "真意")))
        assertEquals("[Player] [カテゴリ] 内容\n  [真意]", io.messages.last().second)
    }

    @Test
    fun `watchEpilogue joins multiple chronicles with newline`() {
        val io = CapturingIO()
        val player = HumanPlayer(Role.VILLAGER, "Player", io)
        player.watchEpilogue(
            listOf(
                ChronicleView.Observation("Player", "カテゴリ", "1件目"),
                ChronicleView.Observation("Player", "カテゴリ", "2件目"),
            )
        )
        assertEquals("[Player] [カテゴリ] 1件目\n[Player] [カテゴリ] 2件目", io.messages.last().second)
    }
}
