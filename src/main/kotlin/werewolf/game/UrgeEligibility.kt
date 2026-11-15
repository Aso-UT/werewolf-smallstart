package werewolf.game

class UrgeEligibility(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility) {
    private val selectableTypes = context.selectableTypes(claimableRoles, reportEligibility)

    val counterClaimRoles: Set<Role> =
        if (StatementType.ROLE_CLAIM in selectableTypes) {
            context.lastTurnStatements.mapNotNull { it.claimedRole }.filter { it in claimableRoles }.toSet()
        } else {
            emptySet()
        }

    val canAnnounce: Boolean = selectableTypes.any { it in Eagerness.ANNOUNCEMENT.statementTypes }

    fun validate(urge: SpeakingUrge) {
        if (urge is CounterClaimSpeakingUrge) {
            require(urge.role in counterClaimRoles) { "${urge.player.name} cannot counter claim ${urge.role}" }
        }
    }
}
