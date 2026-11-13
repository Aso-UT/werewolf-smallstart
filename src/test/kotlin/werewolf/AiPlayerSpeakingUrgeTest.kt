package werewolf

import werewolf.ai.AiPlayer
import werewolf.game.AllPlayers
import werewolf.game.CounterClaimSpeakingUrge
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.Eagerness
import werewolf.game.GameEvent
import werewolf.game.Role
import werewolf.game.Statement

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AiPlayerSpeakingUrgeTest {

    private fun conclaveContext(players: List<AiPlayer>) = DiscussionContext.Conclave(1, 1, players, players)

    @Test
    fun `eagerness uses the last separator when the reason contains a colon`() {
        val lm = FakeLanguageModel("Zackの質問：「Yuriは沈黙していますが、何か意見はないですか？」に答えるため：3")
        val yuri = AiPlayer(Role.VILLAGER, "Yuri", lm, lm, testInstruction("Yuri"))
        assertEquals(Eagerness.URGENT, yuri.speakingUrge(openContext(listOf(yuri))).eagerness)
    }

    private val zoe = ReceivingPlayer(Role.SEER, "Zoe")

    // 直前にZoeが占い師COした議論
    private fun afterSeerClaim(player: AiPlayer) =
        openContext(listOf(player, zoe), previousStatement = Statement.RoleClaim(zoe, Role.SEER))

    @Test
    fun `eagerness returns counter claim level with the role the player answered`() {
        val lm = FakeLanguageModel("占い師COしたZoeに対抗COするため：5（占い師）")
        val madman = AiPlayer(Role.MADMAN, "Madman", lm, lm, testInstruction("Madman"))
        assertEquals(Eagerness.COUNTER_CLAIM, madman.speakingUrge(afterSeerClaim(madman)).eagerness)
        val urge = madman.reveal(fakeCitizenWinSignal()).filterIsInstance<CounterClaimSpeakingUrge>().single()
        assertEquals(Role.SEER, urge.role)
    }

    @Test
    fun `eagerness retries when counter claiming without a role`() {
        val lm = FakeLanguageModel("占い師COしたZoeに対抗COするため：5", "占い師COしたZoeに対抗COするため：5（占い師）")
        val madman = AiPlayer(Role.MADMAN, "Madman", lm, lm, testInstruction("Madman"))
        assertEquals(Eagerness.COUNTER_CLAIM, madman.speakingUrge(afterSeerClaim(madman)).eagerness)
        assertEquals(2, lm.instructions.size)
    }

    @Test
    fun `eagerness retries when counter claiming a role other than the one just claimed`() {
        val lm = FakeLanguageModel("霊能者として対抗COするため：5（霊能者）", "占い師COしたZoeに対抗COするため：5（占い師）")
        val madman = AiPlayer(Role.MADMAN, "Madman", lm, lm, testInstruction("Madman"))
        assertEquals(Eagerness.COUNTER_CLAIM, madman.speakingUrge(afterSeerClaim(madman)).eagerness)
        assertEquals(2, lm.instructions.size)
    }

    @Test
    fun `eagerness retries when a role is added to a level other than counter claim`() {
        val lm = FakeLanguageModel("占い師COするため：4（占い師）", "占い師COするため：4")
        val madman = AiPlayer(Role.MADMAN, "Madman", lm, lm, testInstruction("Madman"))
        assertEquals(Eagerness.ANNOUNCEMENT, madman.speakingUrge(afterSeerClaim(madman)).eagerness)
        assertEquals(2, lm.instructions.size)
    }

    @Test
    fun `counter claim level cannot be chosen when the previous statement claims no role`() {
        val lm = FakeLanguageModel("占い師COしたZoeに対抗COするため：5（占い師）", "占い師COするため：4")
        val madman = AiPlayer(Role.MADMAN, "Madman", lm, lm, testInstruction("Madman"))
        assertEquals(Eagerness.ANNOUNCEMENT, madman.speakingUrge(openContext(listOf(madman, zoe))).eagerness)
        assertFalse(lm.instructions.first().contains("対抗COする（"))
        assertFalse(lm.instructions.first().contains("対抗COする場合は"))
    }

    @Test
    fun `eagerness retries when level is out of the scale`() {
        val lm = FakeLanguageModel("理由：6", "理由：1")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, lm, testInstruction())
        assertEquals(Eagerness.NOTHING, villager.speakingUrge(openContext(listOf(villager))).eagerness)
    }

    @Test
    fun `eagerness falls back to default after two invalid responses`() {
        val lm = FakeLanguageModel("no separator", "理由：たくさん")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, lm, testInstruction())
        assertEquals(Eagerness.DEFAULT, villager.speakingUrge(openContext(listOf(villager))).eagerness)
    }

    @Test
    fun `counter claim level cannot be chosen in conclave`() {
        val lm = FakeLanguageModel("対抗COするため：5", "仲間と相談したいため：2")
        val wolf = AiPlayer(Role.WEREWOLF, "Wolf", lm, lm, testInstruction("Wolf"))
        val context = conclaveContext(listOf(wolf))
        assertEquals(Eagerness.OPINION, wolf.speakingUrge(context).eagerness)
        assertFalse(lm.instructions.first().contains("対抗COする（"))
    }

    @Test
    fun `counter claim and announcement levels cannot be chosen by a player who can neither claim nor report`() {
        val lm = FakeLanguageModel("占い師COしたZoeに対抗COするため：5", "Zoeの占い結果に意見したいため：2")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, lm, testInstruction())
        assertEquals(Eagerness.OPINION, villager.speakingUrge(afterSeerClaim(villager)).eagerness)
        assertFalse(lm.instructions.first().contains("対抗COする（"))
        assertFalse(lm.instructions.first().contains("役職COや、"))
    }

    @Test
    fun `eagerness prompt lists counter claim level with the role that can be counter claimed`() {
        val lm = FakeLanguageModel("占い師COしたZoeに、真の占い師として対抗COするため：5（占い師）")
        val seer = AiPlayer(Role.SEER, "Seer", lm, lm, testInstruction("Seer"))
        seer.speakingUrge(afterSeerClaim(seer))
        assertContains(lm.instructions.first(), "5：対抗COする（")
        assertContains(lm.instructions.first(), "対抗COできる役職：占い師）")
        assertContains(lm.instructions.first(), "対抗COする場合は「理由：5（役職名）」の形式で答えてください。")
    }

    @Test
    fun `announcement level can be chosen to report a result after the role is already claimed`() {
        val lm = FakeLanguageModel("Wolfの占い結果を報告するため：4")
        val seer = AiPlayer(Role.SEER, "Seer", lm, lm, testInstruction("Seer"))
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val allPlayers = AllPlayers(TestLodge(seer to Role.SEER, wolf to Role.WEREWOLF).create().playerManager)
        GameEvent.StatementMade.send(1, seer.name, Statement.RoleClaim(seer, Role.SEER), allPlayers)
        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, seer)
        assertEquals(Eagerness.ANNOUNCEMENT, seer.speakingUrge(openContext(listOf(seer, wolf))).eagerness)
        assertFalse(lm.instructions.first().contains("対抗COする（"))
    }

    @Test
    fun `speaking urge is not included in later prompts`() {
        val lm = FakeLanguageModel(
            "Zackの発言の矛盾を指摘したいため：2",
            "発言する：Zackさん、昨日は占い師を信じると言っていましたよね？[Zackの矛盾を指摘するため]",
        )
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, lm, testInstruction())
        val context = openContext(listOf(villager))
        villager.speakingUrge(context)
        villager.discuss(context)
        assertFalse(lm.histories[1].any { it.contains("Zackの発言の矛盾を指摘したいため") })
    }

    @Test
    fun `eagerness is asked to the urge model and statements to the main model`() {
        val lm = FakeLanguageModel("発言する：Zackさん、昨日は占い師を信じると言っていましたよね？[Zackの矛盾を指摘するため]")
        val urgeLm = FakeLanguageModel("Zackの発言の矛盾を指摘したいため：2")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, urgeLm, testInstruction())
        val context = openContext(listOf(villager))
        villager.speakingUrge(context)
        villager.discuss(context)
        assertEquals(1, urgeLm.instructions.size)
        assertEquals(1, lm.instructions.size)
    }
}
