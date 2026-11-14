package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
    private val another = NothingPlayer(Role.MEDIUM, "Another")
    private val madmanClaimable = setOf(Role.SEER, Role.MEDIUM, Role.HUNTER, Role.WEREWOLF, Role.MADMAN)

    private fun eligibility(
        vararg previousStatements: Statement,
        claimableRoles: Set<Role> = madmanClaimable,
        role: Role = Role.MADMAN,
        conclave: Boolean = false,
    ): UrgeEligibility {
        val players = listOf(self, claimer, another)
        val context = if (conclave) {
            DiscussionContext.Conclave(1, 1, players, players, previousStatements.toList())
        } else {
            DiscussionContext.Open(1, 1, players, players, previousStatements.toList())
        }
        return UrgeEligibility(context, claimableRoles, ReportEligibility.forRole(role, emptyList(), self))
    }

    @Test
    fun `can counter claim the role just claimed when the player can claim it`() {
        assertEquals(setOf(Role.SEER), eligibility(Statement.RoleClaim(claimer, Role.SEER)).counterClaimRoles)
    }

    @Test
    fun `a divination report counts as a seer claim to counter`() {
        val report = Statement.DivinationReport(claimer, self, DivineResult.WEREWOLF)
        assertEquals(setOf(Role.SEER), eligibility(report).counterClaimRoles)
    }

    @Test
    fun `a medium report counts as a medium claim to counter`() {
        val report = Statement.MediumReport(claimer, self, MediumResult.WEREWOLF)
        assertEquals(setOf(Role.MEDIUM), eligibility(report).counterClaimRoles)
    }

    @Test
    fun `can counter claim any role claimed in a simultaneous statement`() {
        val result = eligibility(Statement.RoleClaim(claimer, Role.SEER), Statement.RoleClaim(another, Role.MEDIUM))
        assertEquals(setOf(Role.SEER, Role.MEDIUM), result.counterClaimRoles)
    }

    @Test
    fun `can counter claim only the roles the player can claim in a simultaneous statement`() {
        val result = eligibility(
            Statement.RoleClaim(claimer, Role.SEER),
            Statement.RoleClaim(another, Role.MEDIUM),
            claimableRoles = setOf(Role.SEER),
            role = Role.SEER,
        )
        assertEquals(setOf(Role.SEER), result.counterClaimRoles)
    }

    @Test
    fun `cannot counter claim a role the player cannot claim`() {
        val result = eligibility(Statement.RoleClaim(claimer, Role.MEDIUM), claimableRoles = setOf(Role.SEER), role = Role.SEER)
        assertTrue(result.counterClaimRoles.isEmpty())
    }

    @Test
    fun `cannot counter claim after a plain statement`() {
        assertTrue(eligibility(Statement.Plain(claimer, "おはよう")).counterClaimRoles.isEmpty())
    }

    @Test
    fun `cannot counter claim at the start of a discussion`() {
        assertTrue(eligibility().counterClaimRoles.isEmpty())
    }

    @Test
    fun `cannot counter claim or announce in conclave`() {
        val result = eligibility(Statement.RoleClaim(claimer, Role.SEER), conclave = true)
        assertTrue(result.counterClaimRoles.isEmpty())
        assertFalse(result.canAnnounce)
    }

    @Test
    fun `can announce when the player can claim a role`() {
        assertTrue(eligibility().canAnnounce)
    }

    @Test
    fun `cannot announce when the player can neither claim nor report`() {
        assertFalse(eligibility(claimableRoles = emptySet(), role = Role.VILLAGER).canAnnounce)
    }
}
