package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.MediumResult
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.game.UrgeEligibility

class UrgeEligibilityTest {
    private val self = NothingPlayer(Role.MADMAN, "Self")
    private val claimer = NothingPlayer(Role.SEER, "Claimer")
    private val madmanClaimable = setOf(Role.SEER, Role.MEDIUM, Role.HUNTER, Role.WEREWOLF, Role.MADMAN)

    private fun eligibility(
        previousStatement: Statement?,
        claimableRoles: Set<Role> = madmanClaimable,
        role: Role = Role.MADMAN,
        conclave: Boolean = false,
    ): UrgeEligibility {
        val players = listOf(self, claimer)
        val context = if (conclave) {
            DiscussionContext.Conclave(1, 1, players, players, previousStatement)
        } else {
            DiscussionContext.Open(1, 1, players, players, previousStatement)
        }
        return UrgeEligibility(context, claimableRoles, ReportEligibility.forRole(role, emptyList(), self))
    }

    @Test
    fun `can counter claim the role just claimed when the player can claim it`() {
        assertEquals(Role.SEER, eligibility(Statement.RoleClaim(claimer, Role.SEER)).counterClaimRole)
    }

    @Test
    fun `a divination report counts as a seer claim to counter`() {
        val report = Statement.DivinationReport(claimer, self, DivineResult.WEREWOLF)
        assertEquals(Role.SEER, eligibility(report).counterClaimRole)
    }

    @Test
    fun `a medium report counts as a medium claim to counter`() {
        val report = Statement.MediumReport(claimer, self, MediumResult.WEREWOLF)
        assertEquals(Role.MEDIUM, eligibility(report).counterClaimRole)
    }

    @Test
    fun `cannot counter claim a role the player cannot claim`() {
        val result = eligibility(Statement.RoleClaim(claimer, Role.MEDIUM), claimableRoles = setOf(Role.SEER), role = Role.SEER)
        assertNull(result.counterClaimRole)
    }

    @Test
    fun `cannot counter claim after a plain statement`() {
        assertNull(eligibility(Statement.Plain(claimer, "おはよう")).counterClaimRole)
    }

    @Test
    fun `cannot counter claim at the start of a discussion`() {
        assertNull(eligibility(previousStatement = null).counterClaimRole)
    }

    @Test
    fun `cannot counter claim or announce in conclave`() {
        val result = eligibility(Statement.RoleClaim(claimer, Role.SEER), conclave = true)
        assertNull(result.counterClaimRole)
        assertFalse(result.canAnnounce)
    }

    @Test
    fun `can announce when the player can claim a role`() {
        assertTrue(eligibility(previousStatement = null).canAnnounce)
    }

    @Test
    fun `cannot announce when the player can neither claim nor report`() {
        assertFalse(eligibility(previousStatement = null, claimableRoles = emptySet(), role = Role.VILLAGER).canAnnounce)
    }
}
