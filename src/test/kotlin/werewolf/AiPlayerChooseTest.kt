package werewolf

import werewolf.ai.AiPlayer
import werewolf.ai.InvalidAiInput
import werewolf.ai.ModelMetadata
import werewolf.game.Choice
import werewolf.game.Role
import werewolf.game.SelectionContext

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiPlayerChooseTest {

    @Test
    fun `selectTarget returns matching player from reason-colon-name format`() {
        val lm = FakeLanguageModel("怪しいから：Wolf")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget uses last separator when reason contains colon`() {
        val lm = FakeLanguageModel("理由A：理由B：Wolf")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget retries when player name is not a candidate`() {
        val lm = FakeLanguageModel("理由：Unknown", "怪しいから：Wolf")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget falls back to random candidate after two invalid responses`() {
        val lm = FakeLanguageModel("理由：Unknown", "理由：AlsoUnknown")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertTrue(result in context.candidates())
    }

    @Test
    fun `selectTarget prompt includes format instruction`() {
        val lm = FakeLanguageModel("怪しいから：Wolf")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        villager.selectTarget(context)
        assertContains(lm.prompts.first(), "選んだ理由（200文字以内）：候補名")
    }

    @Test
    fun `selectTarget retries when response has no separator`() {
        val lm = FakeLanguageModel("no separator", "怪しいから：Wolf")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `choose metadata is included in intentForChronicle but not in recall`() {
        val lm = FakeLanguageModel("怪しいから：Wolf", "[dummy]", metadata = ModelMetadata { "model=test" })
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        villager.selectTarget(context)
        villager.discuss(openContext())
        val memories = villager.reveal(fakeCitizenWinSignal())
        assertTrue(memories.filterIsInstance<Choice>().any { it.intentForChronicle?.contains("model=test") == true })
        assertFalse(lm.prompts[1].contains("model=test"))
    }

    @Test
    fun `choose records InvalidAiInput with raw response when format is invalid`() {
        val lm = FakeLanguageModel("bad response", "怪しいから：Wolf", metadata = ModelMetadata { "model=test" })
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        villager.selectTarget(context)
        val memories = villager.reveal(fakeCitizenWinSignal())
        val record = memories.filterIsInstance<InvalidAiInput>().first()
        assertTrue(record.chronicle().contains("bad response"))
        assertEquals("model=test", record.intentForChronicle)
    }
}
