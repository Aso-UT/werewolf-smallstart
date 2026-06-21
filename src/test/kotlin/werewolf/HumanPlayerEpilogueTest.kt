package werewolf

import werewolf.game.*
import werewolf.human.HumanPlayer
import werewolf.human.PlayerIO
import werewolf.phase.Conclave
import werewolf.phase.Epilogue
import werewolf.phase.OpenDiscussion

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HumanPlayerEpilogueTest {

    private class SpeakingIO : PlayerIO() {
        val messages = mutableListOf<Pair<String, String>>()
        override fun sendMessage(title: String, content: String) { messages += title to content }
        override fun readFreeText(): String = "human speaks"
        override fun readChoice(): String = "1"
        override fun readPlayer(): String = error("not used")
    }

    private class SpeakingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun onReceive(event: GameEvent) {}
        override fun watchEpilogue(chronicles: List<ChronicleView>) {}
        override fun speak(context: DiscussionContext): Claim =
            Claim(this, context, Statement.Plain("$name speaks"), "intent")
    }

    @Test
    fun `claims from discussion appear in epilogue but StatementMade does not`() {
        val io = SpeakingIO()
        val human = HumanPlayer(Role.VILLAGER, "Human", io)
        val speaker = SpeakingPlayer(Role.VILLAGER, "Speaker")
        val setup = TestLodge(human to Role.VILLAGER, speaker to Role.VILLAGER).create()

        OpenDiscussion(setup.playerManager, day = 1).conduct()
        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        val content = io.messages.last().second
        assertTrue(content.contains("[Speaker] [議論]"))
        assertFalse(content.contains("発言（"))  // StatementMade のタイトルパターン
    }

    @Test
    fun `claims from conclave appear in epilogue but WerewolfStatementMade does not`() {
        val io = SpeakingIO()
        val human = HumanPlayer(Role.WEREWOLF, "Human", io)
        val wolf = SpeakingPlayer(Role.WEREWOLF, "Wolf")
        val setup = TestLodge(human to Role.WEREWOLF, wolf to Role.WEREWOLF).create()

        Conclave(setup.oracle, setup.playerManager, day = 1).conduct()
        Epilogue(setup.playerManager, setup.oracle, fakeCitizenWinSignal()).perform()

        val content = io.messages.last().second
        assertTrue(content.contains("[Wolf] [密談]"))
        assertFalse(content.contains("密談（"))  // WerewolfStatementMade のタイトルパターン
    }
}
