package werewolf.game

object RoleClaimPolicy {
    fun claimableRoles(role: Role, alreadyRevealed: Boolean): Set<Role> = when (role) {
        Role.VILLAGER -> emptySet()
        Role.SEER, Role.MEDIUM, Role.HUNTER -> denyReCo(trueRoleOnly(role), alreadyRevealed)
        Role.WEREWOLF, Role.MADMAN -> Role.entries.toSet() - Role.VILLAGER
    }

    // 本当の役職しかCOできない
    private fun trueRoleOnly(role: Role): Set<Role> = setOf(role)

    // CO済みなら再COを許さない
    private fun denyReCo(claimable: Set<Role>, alreadyRevealed: Boolean): Set<Role> =
        if (alreadyRevealed) emptySet() else claimable
}
