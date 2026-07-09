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
            Claim(speaker, context, statement, emptySet(), "意図")
        }
    }

    @Test
    fun `Claim does not throw when statement type is available`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        assertNotNull(Claim(speaker, context, Statement.Plain("発言"), emptySet(), "意図"))
    }

    @Test
    fun `Claim throws when statement is RoleClaim for a role that is not claimable`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val statement = Statement.RoleClaim(speaker, Role.SEER)
        assertFailsWith<IllegalArgumentException> {
            Claim(speaker, context, statement, emptySet(), "意図")
        }
    }

    @Test
    fun `Claim does not throw when statement is RoleClaim for a claimable role`() {
        val speaker = NothingPlayer(Role.SEER, "Speaker")
        val context = openContext(listOf(speaker))
        val statement = Statement.RoleClaim(speaker, Role.SEER)
        assertNotNull(Claim(speaker, context, statement, setOf(Role.SEER), "意図"))
    }

    @Test
    fun `toRecallView returns action with context title, content and intent`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain("発言内容"), emptySet(), "真意内容")
        assertEquals(RecallView.SelfAction("議論", "発言内容", "真意内容"), claim.toRecallView())
    }

    @Test
    fun `toChronicleView returns action with speaker, context title, content and intent`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain("発言内容"), emptySet(), "真意内容")
        assertEquals(ChronicleView.Action("Speaker", "議論", "発言内容", "真意内容"), claim.toChronicleView())
    }

    @Test
    fun `DivinationReport text appends comment when present and omits it when blank`() {
        val speaker = NothingPlayer(Role.SEER, "Speaker")
        val target = NothingPlayer(Role.VILLAGER, "Target")
        val withComment = Statement.DivinationReport(speaker, target, DivineResult.WEREWOLF, "怪しいと思っていました")
        val withoutComment = Statement.DivinationReport(speaker, target, DivineResult.WEREWOLF)
        assertEquals("Target は「人狼」です。 怪しいと思っていました", withComment.text())
        assertEquals("Target は「人狼」です。", withoutComment.text())
    }

    @Test
    fun `MediumReport text appends comment when present and omits it when blank`() {
        val speaker = NothingPlayer(Role.MEDIUM, "Speaker")
        val target = NothingPlayer(Role.VILLAGER, "Target")
        val withComment = Statement.MediumReport(speaker, target, MediumResult.NOT_WEREWOLF, "無実の方を処刑してしまい申し訳ない気持ちです")
        val withoutComment = Statement.MediumReport(speaker, target, MediumResult.NOT_WEREWOLF)
        assertEquals("Target は「人狼でない」です。 無実の方を処刑してしまい申し訳ない気持ちです", withComment.text())
        assertEquals("Target は「人狼でない」です。", withoutComment.text())
    }

    @Test
    fun `RoleClaim text appends comment when present and omits it when blank`() {
        val speaker = NothingPlayer(Role.MADMAN, "Speaker")
        val withComment = Statement.RoleClaim(speaker, Role.SEER, "信じてください")
        val withoutComment = Statement.RoleClaim(speaker, Role.SEER)
        assertEquals("私の役職は「占い師」です。 信じてください", withComment.text())
        assertEquals("私の役職は「占い師」です。", withoutComment.text())
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
