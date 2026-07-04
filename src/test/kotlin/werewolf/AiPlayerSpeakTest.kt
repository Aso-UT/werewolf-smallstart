package werewolf

import werewolf.ai.AiPlayer
import werewolf.ai.InvalidAiInput
import werewolf.ai.ModelMetadata
import werewolf.game.ChronicleView
import werewolf.game.Claim
import werewolf.game.Role
import werewolf.game.Statement

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AiPlayerSpeakTest {

    @Test
    fun `discuss extracts statement before bracket from input`() {
        val lm = FakeLanguageModel("hello[真意]")
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
        val lm = FakeLanguageModel("no bracket", "hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss prompt includes format instruction`() {
        val lm = FakeLanguageModel("hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "ゲーム上の発言（50文字以内）[発言の真意（50文字以内）]")
    }

    @Test
    fun `speak metadata is included in intentForChronicle but not in recall`() {
        val lm = FakeLanguageModel("hello[真意内容]", "[dummy]", metadata = ModelMetadata { "model=test" })
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
        val lm = FakeLanguageModel("no bracket", "no bracket", "hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        villager.discuss(openContext())
        villager.discuss(openContext())
        assertTrue(lm.histories[2].any { it.contains("回答取得に失敗したため空文字を返却") })
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
