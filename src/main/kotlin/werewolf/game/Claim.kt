package werewolf.game

sealed class Claim(
    val speaker: Player,
    val context: DiscussionContext,
    val statement: Statement,
    private val intent: String,
    override val metadata: ActionMetadata,
) : Recallable(), PlayerAction {
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
) : Claim(speaker, context, statement, intent, metadata) {
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
}

// 回答を得られなかったときは、何も言わなかったことにする。発言権を得た経緯で選べる発言が絞られていても、黙ることは許す。
// 空文字にしないのは、AIの履歴で発言者名だけの行になると、直後に続く指示文がその発言の中身に見えてしまうため
class FallbackClaim(speaker: Player, context: DiscussionContext) : Claim(
    speaker, context, Statement.Plain(speaker, "（発言なし）"),
    intent = "回答取得に失敗したため発言なしとした",
    metadata = FallbackActionMetadata,
)
