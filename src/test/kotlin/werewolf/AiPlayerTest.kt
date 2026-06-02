package werewolf

import werewolf.ai.AiPlayer
import werewolf.ai.Instruction
import werewolf.ai.LanguageModel
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.Role
import werewolf.game.SelectionContext

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class AiPlayerTest {

    @Test
    fun `prompt includes received events`() {
        val lm = FakeLanguageModel("[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        GameEvent.RoleAssigned.send(Role.VILLAGER, villager)
        villager.discuss(openContext())
        assertContains(lm.prompts.first(), "役職通知")
    }

    @Test
    fun `choice recorded in memories appears in next prompt`() {
        val lm = FakeLanguageModel("怪しいから：Wolf", "hello[真意]")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        villager.selectTarget(SelectionContext.Vote(villager, listOf(villager, wolf)))
        villager.discuss(openContext())
        assertContains(lm.prompts[1], "投票")
        assertContains(lm.prompts[1], "Wolf")
    }

    @Test
    fun `claim recorded in memories appears in next prompt`() {
        val lm = FakeLanguageModel("hello[真意内容]", "怪しいから：Wolf")
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
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
    fun `watchEpilogue does not call languageModel`() {
        val lm = FakeLanguageModel()
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        villager.watchEpilogue(emptyList())
        assertTrue(lm.prompts.isEmpty())
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun `discuss throws GameOverSignal when languageModel throws`() {
        val lm = LanguageModel { _, _, _ -> throw Exception("API error") }
        val villager = AiPlayer(Role.VILLAGER, "Villager", lm, testInstruction())
        assertFailsWith<GameOverSignal> { villager.discuss(openContext()) }
    }
}
