package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ClaimTest {

    @Test
    fun `Claim throws when statement type is not available in context`() {
        val speaker = NothingPlayer(Role.WEREWOLF, "Speaker")
        val context = DiscussionContext.Conclave(1, 1, listOf(speaker), listOf(speaker))
        val statement = Statement.DivinationReport(speaker, speaker, DivineResult.WEREWOLF)
        assertFailsWith<IllegalArgumentException> {
            Claim(speaker, context, statement, "意図")
        }
    }

    @Test
    fun `Claim does not throw when statement type is available`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        assertNotNull(Claim(speaker, context, Statement.Plain("発言"), "意図"))
    }

    @Test
    fun `recall returns context title, statement text, and intent in brackets`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain("発言内容"), "真意内容")
        assertEquals("[議論] 発言内容 [真意内容]", claim.recall())
    }

    @Test
    fun `chronicle returns speaker name, context title, statement text, and intent`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain("発言内容"), "真意内容")
        assertEquals("[Speaker] [議論] 発言内容 [真意内容]", claim.chronicle())
    }

    @Test
    fun `FallbackClaim has empty statement and failure reason in chronicle`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = FallbackClaim(speaker, context)
        assertEquals("", claim.statement.text())
        assertContains(claim.intentForChronicle, "失敗")
    }
}
