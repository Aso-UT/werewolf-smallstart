package werewolf

import werewolf.ai.AiPlayer
import werewolf.ai.InvalidAiInput
import werewolf.ai.ModelMetadata
import werewolf.game.ChronicleView
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.MediumResult
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.game.StatementType

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AiPlayerSpeakTest {

    @Test
    fun `discuss extracts statement before bracket from input`() {
        val lm = FakeLanguageModel("発言する：hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss retries when bracket is missing and returns empty string after two failures`() {
        val lm = FakeLanguageModel("no bracket", "no bracket")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("", result.text())
    }

    @Test
    fun `discuss retries and succeeds on second input`() {
        val lm = FakeLanguageModel("no bracket", "発言する：hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss prompt includes format instruction for every available type`() {
        val lm = FakeLanguageModel("発言する：hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "${StatementType.PLAIN.displayName}：ゲーム上の発言（50文字以内）")
        assertContains(
            lm.prompts.first(),
            "${StatementType.DIVINATION_REPORT.displayName}：対象のプレイヤー名/結果（人狼か人狼でない）/補足コメント（省略可）",
        )
        assertContains(
            lm.prompts.first(),
            "${StatementType.MEDIUM_REPORT.displayName}：対象のプレイヤー名/結果（人狼であるか人狼でない）/補足コメント（省略可）",
        )
    }

    @Test
    fun `discuss uses the plain single-type format when only PLAIN is available`() {
        val lm = FakeLanguageModel("hello[真意]")
        val wolf = AiPlayer(Role.WEREWOLF, "Wolf", lm, testInstruction("Wolf"))
        val context = DiscussionContext.Conclave(1, 1, listOf(wolf), listOf(wolf))
        val result = wolf.discuss(context)
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
        assertContains(lm.prompts.first(), "ゲーム上の発言（50文字以内）[発言の真意（50文字以内）]")
    }

    @Test
    fun `speak metadata is included in intentForChronicle but not in recall`() {
        val lm = FakeLanguageModel(
            "発言する：hello[真意内容]", "発言する：dummy[dummy]",
            metadata = ModelMetadata { "model=test" },
        )
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        villager.discuss(openContext())
        villager.discuss(openContext())
        val memories = villager.reveal(fakeCitizenWinSignal())
        val claimChronicle = memories.filterIsInstance<Claim>().first().toChronicleView()
        assertIs<ChronicleView.Action>(claimChronicle)
        assertTrue(claimChronicle.intent.contains("model=test"))
        assertFalse(lm.prompts[1].contains("model=test"))
    }

    @Test
    fun `discuss records FallbackClaim in recall memory for subsequent prompts`() {
        val lm = FakeLanguageModel("no bracket", "no bracket", "発言する：hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        villager.discuss(openContext())
        villager.discuss(openContext())
        assertTrue(lm.histories[2].any { it.contains("回答取得に失敗したため空文字を返却") })
    }

    @Test
    fun `discuss selects DIVINATION_REPORT and builds a report with target result and comment`() {
        val alice = NothingPlayer(Role.WEREWOLF, "Alice")
        val lm = FakeLanguageModel("占い結果を報告する：Alice/人狼/怪しい発言が多かったので[占い師として信頼を得るため]")
        val seer = AiPlayer(Role.SEER, "Seer", lm, testInstruction("Seer"))
        val result = seer.discuss(openContext(listOf(seer, alice)))
        val report = assertIs<Statement.DivinationReport>(result)
        assertEquals(alice, report.target)
        assertEquals(DivineResult.WEREWOLF, report.result)
        assertEquals("怪しい発言が多かったので", report.comment)
    }

    @Test
    fun `discuss selects MEDIUM_REPORT and omits comment when not provided`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val lm = FakeLanguageModel("霊媒結果を報告する：Alice/人狼でない[霊能者として信頼を得るため]")
        val medium = AiPlayer(Role.MEDIUM, "Medium", lm, testInstruction("Medium"))
        val result = medium.discuss(openContext(listOf(medium, alice)))
        val report = assertIs<Statement.MediumReport>(result)
        assertEquals(alice, report.target)
        assertEquals(MediumResult.NOT_WEREWOLF, report.result)
        assertEquals("", report.comment)
    }

    @Test
    fun `discuss falls back when reported target is not a known player`() {
        val alice = NothingPlayer(Role.VILLAGER, "Alice")
        val lm = FakeLanguageModel(
            "占い結果を報告する：Unknown/人狼/コメント[意図]",
            "占い結果を報告する：Unknown/人狼/コメント[意図]",
        )
        val seer = AiPlayer(Role.SEER, "Seer", lm, testInstruction("Seer"))
        val result = seer.discuss(openContext(listOf(seer, alice)))
        assertIs<Statement.Plain>(result)
        assertEquals("", result.text())
    }

    @Test
    fun `speak records InvalidAiInput with raw response when format is invalid`() {
        val lm = FakeLanguageModel("bad response", "[valid]", metadata = ModelMetadata { "model=test" })
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        villager.discuss(openContext())
        val memories = villager.reveal(fakeCitizenWinSignal())
        val invalidInputChronicle = memories.filterIsInstance<InvalidAiInput>().first().toChronicleView()
        assertIs<ChronicleView.Action>(invalidInputChronicle)
        assertTrue(invalidInputChronicle.content.contains("bad response"))
        assertEquals("model=test", invalidInputChronicle.intent)
    }
}
