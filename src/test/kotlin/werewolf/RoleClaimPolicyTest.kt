package werewolf

import werewolf.game.Role
import werewolf.game.RoleClaimPolicy

import kotlin.test.Test
import kotlin.test.assertEquals

class RoleClaimPolicyTest {

    @Test
    fun `villager can never claim any role`() {
        assertEquals(emptySet(), RoleClaimPolicy.claimableRoles(Role.VILLAGER, alreadyRevealed = false))
        assertEquals(emptySet(), RoleClaimPolicy.claimableRoles(Role.VILLAGER, alreadyRevealed = true))
    }

    @Test
    fun `seer medium hunter can claim only own role when not yet revealed`() {
        assertEquals(setOf(Role.SEER), RoleClaimPolicy.claimableRoles(Role.SEER, alreadyRevealed = false))
        assertEquals(setOf(Role.MEDIUM), RoleClaimPolicy.claimableRoles(Role.MEDIUM, alreadyRevealed = false))
        assertEquals(setOf(Role.HUNTER), RoleClaimPolicy.claimableRoles(Role.HUNTER, alreadyRevealed = false))
    }

    @Test
    fun `seer medium hunter cannot claim again once already revealed`() {
        assertEquals(emptySet(), RoleClaimPolicy.claimableRoles(Role.SEER, alreadyRevealed = true))
        assertEquals(emptySet(), RoleClaimPolicy.claimableRoles(Role.MEDIUM, alreadyRevealed = true))
        assertEquals(emptySet(), RoleClaimPolicy.claimableRoles(Role.HUNTER, alreadyRevealed = true))
    }

    @Test
    fun `werewolf and madman can claim any non-villager role regardless of prior claims`() {
        val expected = Role.entries.toSet() - Role.VILLAGER
        assertEquals(expected, RoleClaimPolicy.claimableRoles(Role.WEREWOLF, alreadyRevealed = false))
        assertEquals(expected, RoleClaimPolicy.claimableRoles(Role.WEREWOLF, alreadyRevealed = true))
        assertEquals(expected, RoleClaimPolicy.claimableRoles(Role.MADMAN, alreadyRevealed = false))
        assertEquals(expected, RoleClaimPolicy.claimableRoles(Role.MADMAN, alreadyRevealed = true))
    }
}
