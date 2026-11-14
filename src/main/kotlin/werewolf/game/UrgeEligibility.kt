package werewolf.game

class UrgeEligibility(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility) {
    private val selectableTypes = context.selectableTypes(claimableRoles, reportEligibility)

    // 直前の発言で名乗られた役職（結果報告を含む）を自分もCOできるときだけ、その役職で対抗COできる
    val counterClaimRole: Role? = context.previousStatement
        ?.claimedRole
        ?.takeIf { StatementType.ROLE_CLAIM in selectableTypes && it in claimableRoles }

    val canAnnounce: Boolean = selectableTypes.any { it in Eagerness.ANNOUNCEMENT.statementTypes }

    fun validate(urge: SpeakingUrge) {
        if (urge is CounterClaimSpeakingUrge) {
            require(urge.role == counterClaimRole) { "${urge.player.name} cannot counter claim ${urge.role}" }
        }
    }
}
