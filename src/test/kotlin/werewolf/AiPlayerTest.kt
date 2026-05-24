package werewolf

import werewolf.game.*
import werewolf.ai.*

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AiPlayerTest {

    private class FakeLanguageModel(
        vararg responses: String,
        private val metadata: ModelMetadata = ModelMetadata { "" },
    ) : LanguageModel {
        private val responseQueue = ArrayDeque(responses.toList())
        val prompts = mutableListOf<String>()

        override fun ask(system: String, user: String): Completion {
            prompts.add(user)
            return Completion(responseQueue.removeFirst(), metadata)
        }
    }

    @Test
    fun `discuss extracts statement before bracket from input`() {
        val lm = FakeLanguageModel("hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss retries when bracket is missing and returns empty string after two failures`() {
        val lm = FakeLanguageModel("no bracket", "no bracket")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("", result.text())
    }

    @Test
    fun `discuss retries and succeeds on second input`() {
        val lm = FakeLanguageModel("no bracket", "hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss prompt includes format instruction`() {
        val lm = FakeLanguageModel("hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "ゲーム上の発言（100文字以内）[発言の真意（100文字以内）]")
    }

    @Test
    fun `selectTarget returns matching player from name-colon-reason format`() {
        val lm = FakeLanguageModel("Wolf：怪しいから")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget retries when player name is not a candidate`() {
        val lm = FakeLanguageModel("Unknown：理由", "Wolf：怪しいから")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget falls back to random candidate after two invalid responses`() {
        val lm = FakeLanguageModel("Unknown：理由", "AlsoUnknown：理由")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertTrue(result in context.candidates())
    }

    @Test
    fun `selectTarget prompt includes format instruction`() {
        val lm = FakeLanguageModel("Wolf：怪しいから")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        villager.selectTarget(context)
        assertContains(lm.prompts.first(), "候補名：選んだ理由")
    }

    @Test
    fun `prompt includes received events`() {
        val lm = FakeLanguageModel("[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        GameEvent.RoleAssigned.send(Role.VILLAGER, villager)
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "役職通知")
    }

    @Test
    fun `selectTarget retries when response has no separator`() {
        val lm = FakeLanguageModel("no separator", "Wolf：怪しいから")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `choice recorded in memories appears in next prompt`() {
        val lm = FakeLanguageModel("Wolf：怪しいから", "hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        villager.selectTarget(SelectionContext.Vote(villager, listOf(villager, wolf)))
        villager.discuss(openContext())
        assertContains(lm.prompts[1], "投票")
        assertContains(lm.prompts[1], "Wolf")
    }

    @Test
    fun `claim recorded in memories appears in next prompt`() {
        val lm = FakeLanguageModel("hello[真意内容]", "Wolf：怪しいから")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        villager.discuss(openContext())
        villager.selectTarget(SelectionContext.Vote(villager, listOf(villager, wolf)))
        assertContains(lm.prompts[1], "議論")
        assertContains(lm.prompts[1], "hello")
        assertContains(lm.prompts[1], "真意内容")
    }

    @Test
    fun `instruction appears in prompt`() {
        val lm = FakeLanguageModel("[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, Instruction("Villager", "慎重に行動してください。"))
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "慎重に行動してください。")
    }

    @Test
    fun `instruction is included in reveal`() {
        val lm = FakeLanguageModel()
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, Instruction("Villager", "慎重に行動してください。"))
        val memories = villager.reveal(fakeCitizenWinSignal())
        assertTrue(memories.any { it.chronicle().contains("慎重に行動してください。") })
    }

    @Test
    fun `instruction chronicle includes recipient name`() {
        val lm = FakeLanguageModel()
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, Instruction("Villager", "慎重に行動してください。"))
        val memories = villager.reveal(fakeCitizenWinSignal())
        assertTrue(memories.any { it.chronicle().contains("Villager") && it.chronicle().contains("慎重に行動してください。") })
    }

    @Test
    fun `speak metadata is included in intentForChronicle but not in recall`() {
        val lm = FakeLanguageModel("hello[真意内容]", "[dummy]", metadata = ModelMetadata { "model=test" })
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        villager.discuss(openContext())
        villager.discuss(openContext())
        val memories = villager.reveal(fakeCitizenWinSignal())
        assertTrue(memories.filterIsInstance<Claim>().any { it.intentForChronicle?.contains("model=test") == true })
        assertFalse(lm.prompts[1].contains("model=test"))
    }

    @Test
    fun `choose metadata is included in intentForChronicle but not in recall`() {
        val lm = FakeLanguageModel("Wolf：怪しいから", "[dummy]", metadata = ModelMetadata { "model=test" })
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        villager.selectTarget(context)
        villager.discuss(openContext())
        val memories = villager.reveal(fakeCitizenWinSignal())
        assertTrue(memories.filterIsInstance<Choice>().any { it.intentForChronicle?.contains("model=test") == true })
        assertFalse(lm.prompts[1].contains("model=test"))
    }

    @Test
    fun `speak records InvalidAiInput with raw response when format is invalid`() {
        val lm = FakeLanguageModel("bad response", "[valid]", metadata = ModelMetadata { "model=test" })
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        villager.discuss(openContext())
        val memories = villager.reveal(fakeCitizenWinSignal())
        val record = memories.filterIsInstance<InvalidAiInput>().first()
        assertTrue(record.chronicle().contains("bad response"))
        assertEquals("model=test", record.intentForChronicle)
    }

    @Test
    fun `choose records InvalidAiInput with raw response when format is invalid`() {
        val lm = FakeLanguageModel("bad response", "Wolf：怪しいから", metadata = ModelMetadata { "model=test" })
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        villager.selectTarget(context)
        val memories = villager.reveal(fakeCitizenWinSignal())
        val record = memories.filterIsInstance<InvalidAiInput>().first()
        assertTrue(record.chronicle().contains("bad response"))
        assertEquals("model=test", record.intentForChronicle)
    }

    @Test
    fun `watchEpilogue does not call languageModel`() {
        val lm = FakeLanguageModel()
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        villager.watchEpilogue(emptyList())
        assertTrue(lm.prompts.isEmpty())
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun `discuss throws GameOverSignal when languageModel throws`() {
        val lm = LanguageModel { _, _ -> throw Exception("API error") }
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm)
        assertFailsWith<GameOverSignal> { villager.discuss(openContext()) }
    }
}
