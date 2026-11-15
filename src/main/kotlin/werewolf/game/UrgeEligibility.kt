package werewolf.game

class UrgeEligibility(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility) {
    private val selectableTypes = context.selectableTypes(claimableRoles, reportEligibility)

    val counterClaimRoles: Set<Role> =
        context.lastTurn?.mapNotNull { it.claimedRole }.orEmpty().filter { StatementType.RoleClaim(it) in selectableTypes }.toSet()

    val canAnnounce: Boolean = selectableTypes.any { it in Eagerness.ANNOUNCEMENT.statementTypes }

    fun validate(urge: SpeakingUrge) {
        if (urge is CounterClaimSpeakingUrge) {
            require(urge.role in counterClaimRoles) { "${urge.player.name} cannot counter claim ${urge.role}" }
        }
    }
}
