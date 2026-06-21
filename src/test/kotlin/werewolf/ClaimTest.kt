package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertEquals

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
    fun `toRecallView returns action with context title, content and intent`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain("発言内容"), "真意内容")
        assertEquals(RecallView.Action("議論", "発言内容", "真意内容"), claim.toRecallView())
    }

    @Test
    fun `toChronicleView returns action with speaker, context title, content and intent`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain("発言内容"), "真意内容")
        assertEquals(ChronicleView.Action("Speaker", "議論", "発言内容", "真意内容"), claim.toChronicleView())
    }

    @Test
    fun `FallbackClaim has empty statement and failure reason in chronicle`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = FallbackClaim(speaker, context)
        assertEquals("", claim.statement.text())
        val fallbackClaimChronicle = claim.toChronicleView()
        assertIs<ChronicleView.Action>(fallbackClaimChronicle)
        assertContains(fallbackClaimChronicle.intent, "失敗")
    }
}
