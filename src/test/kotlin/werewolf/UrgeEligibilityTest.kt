package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.game.UrgeEligibility

class UrgeEligibilityTest {
    private val self = NothingPlayer(Role.MADMAN, "Self")
    private val claimer = NothingPlayer(Role.SEER, "Claimer")

    private fun eligibility(
        previousStatement: Statement?,
        claimableRoles: Set<Role>,
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
        val result = eligibility(Statement.RoleClaim(claimer, Role.SEER), claimableRoles = setOf(Role.SEER, Role.MEDIUM, Role.HUNTER))
        assertEquals(Role.SEER, result.counterClaimRole)
    }

    @Test
    fun `cannot counter claim a role the player cannot claim`() {
        val result = eligibility(Statement.RoleClaim(claimer, Role.MEDIUM), claimableRoles = setOf(Role.SEER), role = Role.SEER)
        assertNull(result.counterClaimRole)
    }

    @Test
    fun `cannot counter claim when the previous statement is not a role claim`() {
        val report = Statement.DivinationReport(claimer, self, DivineResult.WEREWOLF)
        val result = eligibility(report, claimableRoles = setOf(Role.SEER, Role.MEDIUM, Role.HUNTER))
        assertNull(result.counterClaimRole)
    }

    @Test
    fun `cannot counter claim at the start of a discussion`() {
        val result = eligibility(previousStatement = null, claimableRoles = setOf(Role.SEER, Role.MEDIUM, Role.HUNTER))
        assertNull(result.counterClaimRole)
    }

    @Test
    fun `cannot counter claim or announce in conclave`() {
        val result = eligibility(Statement.RoleClaim(claimer, Role.SEER), claimableRoles = setOf(Role.SEER), conclave = true)
        assertNull(result.counterClaimRole)
        assertFalse(result.canAnnounce)
    }

    @Test
    fun `can announce when the player can claim a role`() {
        assertTrue(eligibility(previousStatement = null, claimableRoles = setOf(Role.SEER)).canAnnounce)
    }

    @Test
    fun `cannot announce when the player can neither claim nor report`() {
        assertFalse(eligibility(previousStatement = null, claimableRoles = emptySet(), role = Role.VILLAGER).canAnnounce)
    }
}
