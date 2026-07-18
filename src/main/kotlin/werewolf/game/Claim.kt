package werewolf.game

@Suppress("LongParameterList")
sealed class Claim(
    val speaker: Player,
    val context: DiscussionContext,
    val statement: Statement,
    claimableRoles: Set<Role>,
    reportEligibility: ReportEligibility,
    private val intentForRecall: String,
    private val intentForChronicle: String,
) : Recallable() {
    init {
        require(statement.type in context.availableTypes) {
            "${statement.type} is not available in ${context.title}"
        }
        require(statement.type in context.selectableTypes(claimableRoles, reportEligibility)) {
            "${statement.type} is not available for ${speaker.name} now. context:${context.title}, claimable roles: $claimableRoles."
        }
        require(!reportEligibility.disqualifies(statement)) {
            "${statement.type} is not reportable by ${speaker.name} with the given target/result: ${statement.text()}"
        }
        if (statement is Statement.RoleClaim) {
            require(statement.role in claimableRoles) {
                "${statement.role} is not claimable by ${speaker.name}"
            }
        }
    }

    override fun toRecallView() = RecallView.SelfAction(context.title, statement.text(), intentForRecall)
    override fun toChronicleView() = ChronicleView.Action(speaker.name, context.title, statement.text(), intentForChronicle)

    companion object {
        @Suppress("LongParameterList")
        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            claimableRoles: Set<Role>,
            reportEligibility: ReportEligibility,
            intent: String,
        ): Claim = NormalClaim(speaker, context, statement, claimableRoles, reportEligibility, intent, intent)

        @Suppress("LongParameterList")
        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            claimableRoles: Set<Role>,
            reportEligibility: ReportEligibility,
            intentForRecall: String,
            intentForChronicle: String,
        ): Claim = NormalClaim(speaker, context, statement, claimableRoles, reportEligibility, intentForRecall, intentForChronicle)
    }
}

@Suppress("LongParameterList")
private class NormalClaim(
    speaker: Player,
    context: DiscussionContext,
    statement: Statement,
    claimableRoles: Set<Role>,
    reportEligibility: ReportEligibility,
    intentForRecall: String,
    intentForChronicle: String,
) : Claim(speaker, context, statement, claimableRoles, reportEligibility, intentForRecall, intentForChronicle)

class FallbackClaim(speaker: Player, context: DiscussionContext) : Claim(
    speaker, context, Statement.Plain(speaker, ""), emptySet(), ReportEligibility.Honest(),
    intentForRecall = "回答取得に失敗したため空文字を返却",
    intentForChronicle = "回答取得に失敗したため空文字を返却",
)
