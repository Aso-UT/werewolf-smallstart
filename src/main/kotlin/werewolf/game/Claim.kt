package werewolf.game

sealed class Claim(
    val speaker: Player,
    val context: DiscussionContext,
    val statement: Statement,
    claimableRoles: Set<Role>,
    private val intentForRecall: String,
    private val intentForChronicle: String,
) : Recallable() {
    init {
        require(statement.type in context.availableTypes) {
            "${statement.type} is not available in ${context.title}"
        }
        require(statement.type in context.selectableTypes(claimableRoles)) {
            "${statement.type} is not available for ${speaker.name} now. context:${context.title}, claimable roles: $claimableRoles."
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
        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            claimableRoles: Set<Role>,
            intent: String,
        ): Claim = NormalClaim(speaker, context, statement, claimableRoles, intent, intent)

        @Suppress("LongParameterList")
        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            claimableRoles: Set<Role>,
            intentForRecall: String,
            intentForChronicle: String,
        ): Claim = NormalClaim(speaker, context, statement, claimableRoles, intentForRecall, intentForChronicle)
    }
}

private class NormalClaim(
    speaker: Player,
    context: DiscussionContext,
    statement: Statement,
    claimableRoles: Set<Role>,
    intentForRecall: String,
    intentForChronicle: String,
) : Claim(speaker, context, statement, claimableRoles, intentForRecall, intentForChronicle)

class FallbackClaim(speaker: Player, context: DiscussionContext) : Claim(
    speaker, context, Statement.Plain(""), emptySet(),
    intentForRecall = "回答取得に失敗したため空文字を返却",
    intentForChronicle = "回答取得に失敗したため空文字を返却",
)
