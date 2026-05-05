package org.example

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertIs

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
    fun `discuss returns Plain statement from input`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val (result, _) = withIO("hello\n") { villager.discuss(emptyList()) }
        assertIs<Statement.Plain>(result)
        assertEquals("hello", result.text())
    }

    @Test
    fun `selectTarget returns matching player`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val (result, _) = withIO("Wolf\n") { villager.selectTarget(context) }
        assertEquals(wolf, result)
    }

    @Test
    fun `selectTarget retries on invalid input then returns correct player`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val context = SelectionContext.Vote(villager, listOf(villager, wolf))
        val (result, _) = withIO("Unknown\nWolf\n") { villager.selectTarget(context) }
        assertEquals(wolf, result)
    }

    @Test
    fun `prompt includes received events in discuss output`() {
        val villager = PocAiPlayer(Role.VILLAGER, "Villager")
        GameEvent.RoleAssigned.send(Role.VILLAGER, villager)
        val (_, output) = withIO("\n") { villager.discuss(emptyList()) }
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
