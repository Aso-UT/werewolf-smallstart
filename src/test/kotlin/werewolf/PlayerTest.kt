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
            Claim(speakerToSet, context, Statement.Plain(speakerToSet, ""), claimableRoles, reportEligibility, "")
    }

    private class ArbitraryClaimantSetter(
        role: Role,
        name: String,
        private val claimantToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim =
            Claim(this, context, Statement.Plain(claimantToSet, ""), claimableRoles, reportEligibility, "")
    }

    private class SpeakingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim =
            Claim(this, context, Statement.Plain(this, ""), claimableRoles, reportEligibility, "")
    }

    private class CapturingSpeakerPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        var receivedClaimableRoles: Set<Role>? = null
        var receivedReportEligibility: ReportEligibility? = null
        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
            receivedClaimableRoles = claimableRoles
            receivedReportEligibility = reportEligibility
            return Claim(this, context, Statement.Plain(this, ""), claimableRoles, reportEligibility, "")
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
        assertFailsWith<IllegalArgumentException> { player.discuss(context) }
    }

    @Test
    fun `discuss throws when speak returns a claim whose statement has wrong claimant`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitraryClaimantSetter(Role.VILLAGER, "Player", claimantToSet = otherPlayer)
        val context = openContext(listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.discuss(context) }
    }

    @Test
    fun `discuss records claim in player memories`() {
        val player = SpeakingPlayer(Role.VILLAGER, "Player")
        player.discuss(openContext(listOf(player)))
        assertTrue(player.reveal(fakeCitizenWinSignal()).any { it is Claim })
    }

    @Test
    fun `villager cannot claim any role`() {
        val player = CapturingSpeakerPlayer(Role.VILLAGER, "Player")
        player.discuss(openContext(listOf(player)))
        assertEquals(emptySet(), player.receivedClaimableRoles)
    }

    @Test
    fun `seer can claim own role when not yet revealed`() {
        val player = CapturingSpeakerPlayer(Role.SEER, "Player")
        player.discuss(openContext(listOf(player)))
        assertEquals(setOf(Role.SEER), player.receivedClaimableRoles)
    }

    @Test
    fun `seer cannot claim again after already revealed`() {
        val player = CapturingSpeakerPlayer(Role.SEER, "Player")
        val allPlayers = AllPlayers(TestLodge(player to Role.SEER).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.RoleClaim(player, Role.SEER), allPlayers)

        player.discuss(openContext(listOf(player)))

        assertEquals(emptySet(), player.receivedClaimableRoles)
    }

    @Test
    fun `werewolf can keep re-claiming roles after already claiming`() {
        val player = CapturingSpeakerPlayer(Role.WEREWOLF, "Player")
        val allPlayers = AllPlayers(TestLodge(player to Role.WEREWOLF).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.RoleClaim(player, Role.SEER), allPlayers)

        player.discuss(openContext(listOf(player)))

        assertEquals(Role.entries.toSet() - Role.VILLAGER, player.receivedClaimableRoles)
    }
}
