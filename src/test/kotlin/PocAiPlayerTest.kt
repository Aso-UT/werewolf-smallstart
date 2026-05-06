package org.example

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class PocAiPlayerTest {

    private fun <T> withIO(input: String, block: () -> T): Pair<T, String> {
        val originalIn = System.`in`
        val originalOut = System.out
        val buffer = ByteArrayOutputStream()
        System.setIn(ByteArrayInputStream(input.toByteArray()))
        System.setOut(PrintStream(buffer))
        try {
            return block() to buffer.toString()
        } finally {
            System.setIn(originalIn)
            System.setOut(originalOut)
        }
    }

    @Test
    fun `discuss extracts statement before bracket from input`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val (result, _) = withIO("hello[真意]\n") { villager.discuss(openContext()) }
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss retries when bracket is missing and returns empty string after two failures`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val (result, _) = withIO("no bracket\nno bracket\n") { villager.discuss(openContext()) }
        assertIs<Statement.Plain>(result)
        assertEquals("", result.text())
    }

    @Test
    fun `discuss retries and succeeds on second input`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val (result, _) = withIO("no bracket\nhello[真意]\n") { villager.discuss(openContext()) }
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `discuss prompt includes format instruction`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val (_, output) = withIO("hello[真意]\n") { villager.discuss(openContext()) }
        assertContains(output, "ゲーム上の発言（100文字以内）[発言の真意（100文字以内）]")
    }

    @Test
    fun `selectTarget returns matching player from name-colon-reason format`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val (result, _) = withIO("Wolf：怪しいから\n") { villager.selectTarget(context) }
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget retries when player name is not a candidate`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val (result, _) = withIO("Unknown：理由\nWolf：怪しいから\n") { villager.selectTarget(context) }
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget falls back to random candidate after two invalid responses`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val (result, _) = withIO("Unknown：理由\nAlsoUnknown：理由\n") { villager.selectTarget(context) }
        assertTrue(result in context.candidates())
    }

    @Test
    fun `selectTarget prompt includes format instruction`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val (_, output) = withIO("Wolf：怪しいから\n") { villager.selectTarget(context) }
        assertContains(output, "候補名：選んだ理由")
    }

    @Test
    fun `prompt includes received events in discuss output`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        GameEvent.RoleAssigned.send(Role.VILLAGER, villager)
        val (_, output) = withIO("\n") { villager.discuss(openContext()) }
        assertContains(output, "役職通知")
    }

    @Test
    fun `watchEpilogue prints event recipientName, title and body, then prompts for reflection`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        // ① 通常ルートでイベントを送る
        GameEvent.RoleAssigned.send(Role.VILLAGER, villager)
        // ② fakeのGameOverSignalでknowledgeを取り出す
        val events = villager.revealKnowledge(fakeCitizenWinSignal())
        // ③ watchEpilogueに渡して出力を検証（printPromptのreadLine分の入力を用意）
        val (_, output) = withIO("\n") { villager.watchEpilogue(events) }
        assertContains(output, "Villager")
        assertContains(output, "役職通知")
        assertContains(output, "振り返り")
    }
}
