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
            Claim(speaker, context, statement, ReportEligibility.Honest(), "意図", UnimplementedActionMetadata)
        }
    }

    @Test
    fun `Claim does not throw when statement type is available`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        assertNotNull(Claim(speaker, context, Statement.Plain(speaker, "発言"), ReportEligibility.Honest(), "意図", UnimplementedActionMetadata))
    }

    @Test
    fun `Claim throws when the role claim is not among the selectable types`() {
        val speaker = NothingPlayer(Role.SEER, "Speaker")
        val context = openContext(listOf(speaker)).narrowedTo(StatementType.claimingOnly(setOf(Role.SEER)))
        val statement = Statement.RoleClaim(speaker, Role.WEREWOLF)
        val exception = assertFailsWith<IllegalArgumentException> {
            Claim(speaker, context, statement, ReportEligibility.Honest(), "意図", UnimplementedActionMetadata)
        }
        assertContains(exception.message.orEmpty(), "is not available")
    }

    @Test
    fun `Claim does not throw when the role claim is among the selectable types`() {
        val speaker = NothingPlayer(Role.SEER, "Speaker")
        val context = openContext(listOf(speaker)).narrowedTo(StatementType.claimingOnly(setOf(Role.SEER)))
        val statement = Statement.RoleClaim(speaker, Role.SEER)
        assertNotNull(Claim(speaker, context, statement, ReportEligibility.Honest(), "意図", UnimplementedActionMetadata))
    }

    @Test
    fun `Claim throws when reportEligibility disqualifies the statement`() {
        val speaker = NothingPlayer(Role.SEER, "Speaker")
        val target = NothingPlayer(Role.VILLAGER, "Target")
        val context = openContext(listOf(speaker, target))
        val statement = Statement.DivinationReport(speaker, target, DivineResult.WEREWOLF)
        val reportEligibility = ReportEligibility.Honest(divinations = mapOf(target to DivineResult.NOT_WEREWOLF))
        val exception = assertFailsWith<IllegalArgumentException> {
            Claim(speaker, context, statement, reportEligibility, "意図", UnimplementedActionMetadata)
        }
        assertContains(exception.message.orEmpty(), "is not reportable by")
    }

    @Test
    fun `toRecallView returns action with context title, content and intent`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain(speaker, "発言内容"), ReportEligibility.Honest(), "真意内容", UnimplementedActionMetadata)
        assertEquals(RecallView.SelfAction("議論", "発言内容", "真意内容"), claim.toRecallView())
    }

    @Test
    fun `toChronicleView returns action with speaker, context title, content and intent`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain(speaker, "発言内容"), ReportEligibility.Honest(), "真意内容", UnimplementedActionMetadata)
        assertEquals(ChronicleView.Action("Speaker", "議論", "発言内容", "真意内容"), claim.toChronicleView())
    }

    @Test
    fun `metadata display is appended to intent only in chronicle`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = Claim(speaker, context, Statement.Plain(speaker, "発言内容"), ReportEligibility.Honest(), "真意内容", FakeActionMetadata("model=test"))
        assertEquals(RecallView.SelfAction("議論", "発言内容", "真意内容"), claim.toRecallView())
        assertEquals(ChronicleView.Action("Speaker", "議論", "発言内容", "真意内容 | model=test"), claim.toChronicleView())
    }

    @Test
    fun `DivinationReport text appends comment when present and omits it when blank`() {
        val speaker = NothingPlayer(Role.SEER, "Speaker")
        val target = NothingPlayer(Role.VILLAGER, "Target")
        val withComment = Statement.DivinationReport(speaker, target, DivineResult.WEREWOLF, "怪しいと思っていました")
        val withoutComment = Statement.DivinationReport(speaker, target, DivineResult.WEREWOLF)
        assertEquals("Targetを占ったところ、「人狼」でした。 怪しいと思っていました", withComment.text())
        assertEquals("Targetを占ったところ、「人狼」でした。", withoutComment.text())
    }

    @Test
    fun `MediumReport text appends comment when present and omits it when blank`() {
        val speaker = NothingPlayer(Role.MEDIUM, "Speaker")
        val target = NothingPlayer(Role.VILLAGER, "Target")
        val withComment = Statement.MediumReport(speaker, target, MediumResult.NOT_WEREWOLF, "無実の方を処刑してしまい申し訳ない気持ちです")
        val withoutComment = Statement.MediumReport(speaker, target, MediumResult.NOT_WEREWOLF)
        assertEquals("Targetの霊能結果は、「人狼以外」でした。 無実の方を処刑してしまい申し訳ない気持ちです", withComment.text())
        assertEquals("Targetの霊能結果は、「人狼以外」でした。", withoutComment.text())
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
    fun `FallbackClaim states that nothing was said and records failure reason in chronicle`() {
        val speaker = NothingPlayer(Role.VILLAGER, "Speaker")
        val context = openContext(listOf(speaker))
        val claim = FallbackClaim(speaker, context)
        assertEquals("（発言なし）", claim.statement.text())
        val fallbackClaimChronicle = claim.toChronicleView()
        assertIs<ChronicleView.Action>(fallbackClaimChronicle)
        assertContains(fallbackClaimChronicle.intent, "失敗")
    }
}
