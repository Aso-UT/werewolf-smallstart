package werewolf

import werewolf.ai.AiPlayer
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Role

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class AiPlayerSpeakingUrgeTest {

    private fun conclaveContext(players: List<AiPlayer>) = DiscussionContext.Conclave(1, 1, players, players)

    @Test
    fun `eagerness uses the last separator when the reason contains a colon`() {
        val lm = FakeLanguageModel("Zackの質問：「Yuriは沈黙していますが、何か意見はないですか？」に答えるため：3")
        val yuri = AiPlayer(Role.VILLAGER, "Yuri", lm, testInstruction("Yuri"))
        assertEquals(Eagerness(3), yuri.eagerness(openContext(listOf(yuri))))
    }

    @Test
    fun `counter claim level is the same as a human wanting to speak`() {
        val lm = FakeLanguageModel("占い師COしたZoeに対抗COするため：4")
        val madman = AiPlayer(Role.MADMAN, "Madman", lm, testInstruction("Madman"))
        assertEquals(Eagerness.HIGH, madman.eagerness(openContext(listOf(madman))))
    }

    @Test
    fun `eagerness retries when level is out of the scale`() {
        val lm = FakeLanguageModel("理由：5", "理由：1")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        assertEquals(Eagerness(1), villager.eagerness(openContext(listOf(villager))))
    }

    @Test
    fun `eagerness falls back to default after two invalid responses`() {
        val lm = FakeLanguageModel("no separator", "理由：たくさん")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        assertEquals(Eagerness.DEFAULT, villager.eagerness(openContext(listOf(villager))))
    }

    @Test
    fun `counter claim level cannot be chosen in conclave`() {
        val lm = FakeLanguageModel("対抗COするため：4", "仲間と相談したいため：2")
        val wolf = AiPlayer(Role.WEREWOLF, "Wolf", lm, testInstruction("Wolf"))
        val context = conclaveContext(listOf(wolf))
        assertEquals(Eagerness(2), wolf.eagerness(context))
        assertFalse(lm.instructions.first().contains("対抗COする（"))
    }

    @Test
    fun `counter claim level cannot be chosen by a player who cannot claim any role`() {
        val lm = FakeLanguageModel("占い師COしたZoeに対抗COするため：4", "Zoeの占い結果に意見したいため：2")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        assertEquals(Eagerness(2), villager.eagerness(openContext(listOf(villager))))
        assertFalse(lm.instructions.first().contains("対抗COする（"))
    }

    @Test
    fun `eagerness prompt lists counter claim level when the player can claim a role`() {
        val lm = FakeLanguageModel("占い師COしたZoeに、真の占い師として対抗COするため：4")
        val seer = AiPlayer(Role.SEER, "Seer", lm, testInstruction("Seer"))
        seer.eagerness(openContext(listOf(seer)))
        assertContains(lm.instructions.first(), "4：対抗COする（")
    }

    @Test
    fun `speaking urge is not included in later prompts`() {
        val lm = FakeLanguageModel(
            "Zackの発言の矛盾を指摘したいため：2",
            "発言する：Zackさん、昨日は占い師を信じると言っていましたよね？[Zackの矛盾を指摘するため]",
        )
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val context = openContext(listOf(villager))
        villager.eagerness(context)
        villager.discuss(context)
        assertFalse(lm.histories[1].any { it.contains("Zackの発言の矛盾を指摘したいため") })
    }
}
