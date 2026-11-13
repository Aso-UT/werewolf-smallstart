package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import werewolf.game.CounterClaimSpeakingUrge
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.human.HumanPlayer

class HumanPlayerSpeakingUrgeTest {

    // 指定した名前のボタンを押す。null なら何も押さずに時間切れにする
    private class ButtonPressingIO(private val pressing: String?) : NothingHumanIO() {
        var offeredButtons: List<String> = emptyList()
        override fun waitUntilReady() {}
        override fun promptSpeakingUrge(title: String, seconds: Int, buttons: List<String>, timeoutButton: String): String {
            offeredButtons = buttons
            return pressing ?: timeoutButton
        }
    }

    private val seerClaimer = ReceivingPlayer(Role.SEER, "SeerClaimer")

    // 直前に別の人が占い師COした議論
    private fun afterSeerClaim(player: HumanPlayer) =
        openContext(listOf(player, seerClaimer), previousStatement = Statement.RoleClaim(seerClaimer, Role.SEER))

    @Test
    fun `a villager is offered only the speak button`() {
        val io = ButtonPressingIO(pressing = null)
        val villager = HumanPlayer(Role.VILLAGER, "Human", io)

        villager.speakingUrge(afterSeerClaim(villager))

        assertEquals(listOf("発言を希望"), io.offeredButtons)
    }

    @Test
    fun `a player who can claim the role just claimed is offered the counter claim button`() {
        val io = ButtonPressingIO(pressing = null)
        val madman = HumanPlayer(Role.MADMAN, "Human", io)

        madman.speakingUrge(afterSeerClaim(madman))

        assertEquals(listOf("対抗CO（占い師）", "CO・結果報告", "発言を希望"), io.offeredButtons)
    }

    @Test
    fun `pressing the counter claim button gives the highest eagerness with the role to counter`() {
        val madman = HumanPlayer(Role.MADMAN, "Human", ButtonPressingIO(pressing = "対抗CO（占い師）"))

        assertEquals(Eagerness.COUNTER_CLAIM, madman.speakingUrge(afterSeerClaim(madman)).eagerness)
        val urge = madman.reveal(fakeCitizenWinSignal()).filterIsInstance<CounterClaimSpeakingUrge>().single()
        assertEquals(Role.SEER, urge.role)
    }

    @Test
    fun `pressing the announcement button gives the announcement eagerness`() {
        val madman = HumanPlayer(Role.MADMAN, "Human", ButtonPressingIO(pressing = "CO・結果報告"))
        assertEquals(Eagerness.ANNOUNCEMENT, madman.speakingUrge(afterSeerClaim(madman)).eagerness)
    }

    @Test
    fun `pressing the speak button gives an eagerness below announcements and counter claims`() {
        val madman = HumanPlayer(Role.MADMAN, "Human", ButtonPressingIO(pressing = "発言を希望"))
        assertEquals(Eagerness.URGENT, madman.speakingUrge(afterSeerClaim(madman)).eagerness)
    }

    @Test
    fun `timing out without pressing any button gives lower eagerness than the default`() {
        val villager = HumanPlayer(Role.VILLAGER, "Human", ButtonPressingIO(pressing = null))
        assertEquals(Eagerness.UNWILLING, villager.speakingUrge(afterSeerClaim(villager)).eagerness)
    }

    private class CallRecordingIO : NothingHumanIO() {
        val calls = mutableListOf<String>()
        override fun waitUntilReady() { calls.add("waitUntilReady") }
        override fun promptSpeakingUrge(title: String, seconds: Int, buttons: List<String>, timeoutButton: String): String =
            timeoutButton.also { calls.add("promptSpeakingUrge") }
    }

    @Test
    fun `waits until ready before asking so that the wait is not counted as response time`() {
        val io = CallRecordingIO()
        val player = HumanPlayer(Role.VILLAGER, "Human", io)

        player.speakingUrge(DiscussionContext.Open(1, 1, listOf(player), listOf(player)))

        assertEquals(listOf("waitUntilReady", "promptSpeakingUrge"), io.calls)
    }
}
