package werewolf

import werewolf.game.*
import werewolf.ai.*

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PocAiPlayerTest {

    private class FakeLanguageModel(vararg responses: String) : LanguageModel {
        private val responseQueue = ArrayDeque(responses.toList())
        val prompts = mutableListOf<String>()

        override fun ask(prompt: String): String {
            prompts.add(prompt)
            return responseQueue.removeFirst()
        }
    }

    @Test
    fun `discuss extracts statement before bracket from input`() {
        val lm = FakeLanguageModel("hello[真意]")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss retries when bracket is missing and returns empty string after two failures`() {
        val lm = FakeLanguageModel("no bracket", "no bracket")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("", result.text())
    }

    @Test
    fun `discuss retries and succeeds on second input`() {
        val lm = FakeLanguageModel("no bracket", "hello[真意]")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val result = villager.discuss(openContext())
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss prompt includes format instruction`() {
        val lm = FakeLanguageModel("hello[真意]")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "ゲーム上の発言（100文字以内）[発言の真意（100文字以内）]")
    }

    @Test
    fun `selectTarget returns matching player from name-colon-reason format`() {
        val lm = FakeLanguageModel("Wolf：怪しいから")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget retries when player name is not a candidate`() {
        val lm = FakeLanguageModel("Unknown：理由", "Wolf：怪しいから")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget falls back to random candidate after two invalid responses`() {
        val lm = FakeLanguageModel("Unknown：理由", "AlsoUnknown：理由")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertTrue(result in context.candidates())
    }

    @Test
    fun `selectTarget prompt includes format instruction`() {
        val lm = FakeLanguageModel("Wolf：怪しいから")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        villager.selectTarget(context)
        assertContains(lm.prompts.first(), "候補名：選んだ理由")
    }

    @Test
    fun `prompt includes received events`() {
        val lm = FakeLanguageModel("[真意]")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        GameEvent.RoleAssigned.send(Role.VILLAGER, villager)
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "役職通知")
    }

    @Test
    fun `selectTarget retries when response has no separator`() {
        val lm = FakeLanguageModel("no separator", "Wolf：怪しいから")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val result = villager.selectTarget(context)
        assertEquals(wolf, result)
    }

    @Test
    fun `choice recorded in memories appears in next prompt`() {
        val lm = FakeLanguageModel("Wolf：怪しいから", "hello[真意]")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        villager.selectTarget(SelectionContext.Vote(villager, listOf(villager, wolf)))
        villager.discuss(openContext())
        assertContains(lm.prompts[1], "投票")
        assertContains(lm.prompts[1], "Wolf")
    }

    @Test
    fun `claim recorded in memories appears in next prompt`() {
        val lm = FakeLanguageModel("hello[真意内容]", "Wolf：怪しいから")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        villager.discuss(openContext())
        villager.selectTarget(SelectionContext.Vote(villager, listOf(villager, wolf)))
        assertContains(lm.prompts[1], "議論")
        assertContains(lm.prompts[1], "hello")
        assertContains(lm.prompts[1], "真意内容")
    }

    @Test
    fun `watchEpilogue prompt includes chronicle of events and reflection instruction`() {
        val lm = FakeLanguageModel("")
        val villager = PocAiPlayer(Role.VILLAGER, "Villager", lm)
        GameEvent.RoleAssigned.send(Role.VILLAGER, villager)
        val memories = villager.reveal(fakeCitizenWinSignal())
        villager.watchEpilogue(memories)
        assertContains(lm.prompts.first(), "Villager")
        assertContains(lm.prompts.first(), "役職通知")
        assertContains(lm.prompts.first(), "振り返り")
    }
}
