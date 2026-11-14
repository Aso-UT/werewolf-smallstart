package werewolf.game

class UrgeEligibility(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility) {
    private val selectableTypes = context.selectableTypes(claimableRoles, reportEligibility)

    // 直前の発言で名乗られた役職（結果報告を含む）のうち、自分もCOできるものに対抗COできる。
    // 同時発言で複数の役職が名乗られていれば、そのどれにも対抗COできる
    val counterClaimRoles: Set<Role> =
        if (StatementType.ROLE_CLAIM in selectableTypes) {
            context.previousStatements.mapNotNull { it.claimedRole }.filter { it in claimableRoles }.toSet()
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
