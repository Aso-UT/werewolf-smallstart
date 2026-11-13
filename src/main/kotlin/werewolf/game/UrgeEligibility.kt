package werewolf.game

// 発言意欲のうち、発言の内容が決まっている段階（対抗CO、CO・結果報告）を今選べるかを決める
class UrgeEligibility(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility) {
    private val selectableTypes = context.selectableTypes(claimableRoles, reportEligibility)

    // 直前の発言で名乗られた役職（結果報告を含む）を自分もCOできるときだけ、その役職で対抗COできる
    val counterClaimRole: Role? = context.previousStatement
        ?.claimedRole
        ?.takeIf { StatementType.ROLE_CLAIM in selectableTypes && it in claimableRoles }

    val canAnnounce: Boolean = selectableTypes.any { it in ANNOUNCEMENT_TYPES }

    private companion object {
        val ANNOUNCEMENT_TYPES = setOf(StatementType.ROLE_CLAIM, StatementType.DIVINATION_REPORT, StatementType.MEDIUM_REPORT)
    }
}
