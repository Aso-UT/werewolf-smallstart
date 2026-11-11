package werewolf.game

@Suppress("LongParameterList")
sealed class Claim(
    val speaker: Player,
    val context: DiscussionContext,
    val statement: Statement,
    claimableRoles: Set<Role>,
    reportEligibility: ReportEligibility,
    private val intent: String,
    override val metadata: ActionMetadata,
) : Recallable(), PlayerAction {
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

    override fun toRecallView() = RecallView.SelfAction(context.title, statement.text(), intent)
    override fun toChronicleView() = ChronicleView.Action(speaker.name, context.title, statement.text(), intentWithMetadata(intent))

    companion object {
        @Suppress("LongParameterList")
        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            claimableRoles: Set<Role>,
            reportEligibility: ReportEligibility,
            intent: String,
            metadata: ActionMetadata,
        ): Claim = NormalClaim(speaker, context, statement, claimableRoles, reportEligibility, intent, metadata)
    }
}

@Suppress("LongParameterList")
private class NormalClaim(
    speaker: Player,
    context: DiscussionContext,
    statement: Statement,
    claimableRoles: Set<Role>,
    reportEligibility: ReportEligibility,
    intent: String,
    metadata: ActionMetadata,
) : Claim(speaker, context, statement, claimableRoles, reportEligibility, intent, metadata)

class FallbackClaim(speaker: Player, context: DiscussionContext) : Claim(
    speaker, context, Statement.Plain(speaker, ""), emptySet(), ReportEligibility.Honest(),
    intent = "回答取得に失敗したため空文字を返却",
    metadata = FallbackActionMetadata,
)
