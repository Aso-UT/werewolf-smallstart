package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlayerTest {

    private class ArbitraryChooserSetter(
        role: Role,
        name: String,
        private val chooserToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(chooserToSet, context)
    }

    private class ChoosingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(this, context)
    }

    private class ArbitrarySpeakerSetter(
        role: Role,
        name: String,
        private val speakerToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim =
            Claim(speakerToSet, context, Statement.Plain(speakerToSet, ""), claimableRoles, reportEligibility, "", UnimplementedActionMetadata)
    }

    private class ArbitraryClaimantSetter(
        role: Role,
        name: String,
        private val claimantToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim =
            Claim(this, context, Statement.Plain(claimantToSet, ""), claimableRoles, reportEligibility, "", UnimplementedActionMetadata)
    }

    private class SpeakingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim =
            Claim(this, context, Statement.Plain(this, ""), claimableRoles, reportEligibility, "", UnimplementedActionMetadata)
    }

    private class ArbitraryUrgedPlayerSetter(
        role: Role,
        name: String,
        private val playerToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility) =
            SpeakingUrge(playerToSet, context, Eagerness.DEFAULT, "", UnimplementedActionMetadata)
    }

    private class UrgedPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility) =
            SpeakingUrge(this, context, Eagerness(3), "", UnimplementedActionMetadata)
    }

    private class CounterClaimingPlayer(role: Role, name: String, private val counterRole: Role) : NothingPlayer(role, name) {
        override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility) =
            CounterClaimSpeakingUrge(this, context, counterRole, "", UnimplementedActionMetadata)
    }

    private class CapturingSpeakerPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        var receivedClaimableRoles: Set<Role>? = null
        var receivedReportEligibility: ReportEligibility? = null
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
            receivedClaimableRoles = claimableRoles
            receivedReportEligibility = reportEligibility
            return Claim(this, context, Statement.Plain(this, ""), claimableRoles, reportEligibility, "", UnimplementedActionMetadata)
        }
    }

    // 選べる発言の種類とCOできる役職を記録し、COできる役職の一つをCOする
    private class RecordingClaimer(role: Role, name: String) : ReceivingPlayer(role, name) {
        var receivedTypes: Set<StatementType>? = null
        var receivedClaimableRoles: Set<Role>? = null
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
            receivedTypes = context.selectableTypes(claimableRoles, reportEligibility)
            receivedClaimableRoles = claimableRoles
            val statement = Statement.RoleClaim(this, claimableRoles.first())
            return Claim(this, context, statement, claimableRoles, reportEligibility, "", UnimplementedActionMetadata)
        }
    }

    @Test
    fun `selectTarget throws when choose returns a choice with wrong chooser`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitraryChooserSetter(Role.VILLAGER, "Player", chooserToSet = otherPlayer)
        val context = SelectionContext.Vote(player, listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.selectTarget(context) }
    }

    @Test
    fun `selectTarget records choice in player memories`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ChoosingPlayer(Role.VILLAGER, "Player")
        val context = SelectionContext.Vote(player, listOf(player, otherPlayer))
        player.selectTarget(context)
        assertTrue(player.reveal(fakeCitizenWinSignal()).any { it is Choice })
    }

    @Test
    fun `discuss throws when speak returns a claim with wrong speaker`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitrarySpeakerSetter(Role.VILLAGER, "Player", speakerToSet = otherPlayer)
        val context = openContext(listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.discuss(context, SpeakingRight.Uncontested(player)) }
    }

    @Test
    fun `discuss throws when speak returns a claim whose statement has wrong claimant`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitraryClaimantSetter(Role.VILLAGER, "Player", claimantToSet = otherPlayer)
        val context = openContext(listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.discuss(context, SpeakingRight.Uncontested(player)) }
    }

    @Test
    fun `discuss records claim in player memories`() {
        val player = SpeakingPlayer(Role.VILLAGER, "Player")
        player.discuss(openContext(listOf(player)), SpeakingRight.Uncontested(player))
        assertTrue(player.reveal(fakeCitizenWinSignal()).any { it is Claim })
    }

    @Test
    fun `discuss throws when another player has the right to speak`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = SpeakingPlayer(Role.VILLAGER, "Player")
        val context = openContext(listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.discuss(context, SpeakingRight.Uncontested(otherPlayer)) }
    }

    @Test
    fun `uncontested speaker can make any statement`() {
        val player = RecordingClaimer(Role.MADMAN, "Player")
        player.discuss(openContext(listOf(player)), SpeakingRight.Uncontested(player))
        assertEquals(StatementType.entries.toSet(), player.receivedTypes)
    }

    @Test
    fun `speaker who won with an announcement can only claim a role or report results`() {
        val player = RecordingClaimer(Role.MADMAN, "Player")
        val context = openContext(listOf(player))
        val urge = SpeakingUrge(player, context, Eagerness.ANNOUNCEMENT, "", UnimplementedActionMetadata)

        player.discuss(context, SpeakingRight.Won(urge))

        assertEquals(setOf(StatementType.ROLE_CLAIM, StatementType.DIVINATION_REPORT, StatementType.MEDIUM_REPORT), player.receivedTypes)
    }

    @Test
    fun `speaker who won with an announcement cannot make a plain statement`() {
        val player = SpeakingPlayer(Role.MADMAN, "Player")
        val context = openContext(listOf(player))
        val urge = SpeakingUrge(player, context, Eagerness.ANNOUNCEMENT, "", UnimplementedActionMetadata)

        assertFailsWith<IllegalArgumentException> { player.discuss(context, SpeakingRight.Won(urge)) }
    }

    @Test
    fun `speaker who won with a counter claim can only claim that role or report its results`() {
        val player = RecordingClaimer(Role.MADMAN, "Player")
        val context = openContext(listOf(player))
        val urge = CounterClaimSpeakingUrge(player, context, Role.SEER, "", UnimplementedActionMetadata)

        player.discuss(context, SpeakingRight.Won(urge))

        assertEquals(setOf(StatementType.ROLE_CLAIM, StatementType.DIVINATION_REPORT), player.receivedTypes)
        assertEquals(setOf(Role.SEER), player.receivedClaimableRoles)
    }

    @Test
    fun `speakingUrge throws when feelUrge returns an urge of another player`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitraryUrgedPlayerSetter(Role.VILLAGER, "Player", playerToSet = otherPlayer)
        val context = openContext(listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.speakingUrge(context) }
    }

    @Test
    fun `speakingUrge records the urge in player memories and returns it`() {
        val player = UrgedPlayer(Role.VILLAGER, "Player")
        val eagerness = player.speakingUrge(openContext(listOf(player))).eagerness
        assertEquals(Eagerness(3), eagerness)
        assertTrue(player.reveal(fakeCitizenWinSignal()).any { it is SpeakingUrge })
    }

    @Test
    fun `speakingUrge throws when counter claiming a role other than the one just claimed`() {
        val claimer = NothingPlayer(Role.SEER, "Claimer")
        val player = CounterClaimingPlayer(Role.MADMAN, "Player", counterRole = Role.MEDIUM)
        val context = openContext(listOf(player, claimer), previousStatement = Statement.RoleClaim(claimer, Role.SEER))
        assertFailsWith<IllegalArgumentException> { player.speakingUrge(context) }
    }

    @Test
    fun `villager cannot claim any role`() {
        val player = CapturingSpeakerPlayer(Role.VILLAGER, "Player")
        player.discuss(openContext(listOf(player)), SpeakingRight.Uncontested(player))
        assertEquals(emptySet(), player.receivedClaimableRoles)
    }

    @Test
    fun `seer can claim own role when not yet revealed`() {
        val player = CapturingSpeakerPlayer(Role.SEER, "Player")
        player.discuss(openContext(listOf(player)), SpeakingRight.Uncontested(player))
        assertEquals(setOf(Role.SEER), player.receivedClaimableRoles)
    }

    @Test
    fun `seer cannot claim again after already revealed`() {
        val player = CapturingSpeakerPlayer(Role.SEER, "Player")
        val allPlayers = AllPlayers(TestLodge(player to Role.SEER).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.RoleClaim(player, Role.SEER), allPlayers)

        player.discuss(openContext(listOf(player)), SpeakingRight.Uncontested(player))

        assertEquals(emptySet(), player.receivedClaimableRoles)
    }

    @Test
    fun `werewolf can keep re-claiming roles after already claiming`() {
        val player = CapturingSpeakerPlayer(Role.WEREWOLF, "Player")
        val allPlayers = AllPlayers(TestLodge(player to Role.WEREWOLF).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.RoleClaim(player, Role.SEER), allPlayers)

        player.discuss(openContext(listOf(player)), SpeakingRight.Uncontested(player))

        assertEquals(Role.entries.toSet() - Role.VILLAGER, player.receivedClaimableRoles)
    }
}
