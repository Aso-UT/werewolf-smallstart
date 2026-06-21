package werewolf.game

sealed class Claim(
    val speaker: Player,
    val context: DiscussionContext,
    val statement: Statement,
    private val intentForRecall: String,
    private val intentForChronicle: String,
) : Recallable() {
    init {
        require(statement.type in context.availableTypes) { "${statement.type} is not available in ${context.title}" }
    }

    override fun toRecallView() = RecallView.Action(context.title, statement.text(), intentForRecall)
    override fun toChronicleView() = ChronicleView.Action(speaker.name, context.title, statement.text(), intentForChronicle)

    companion object {
        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            intent: String,
        ): Claim = NormalClaim(speaker, context, statement, intent, intent)

        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            intentForRecall: String,
            intentForChronicle: String,
        ): Claim = NormalClaim(speaker, context, statement, intentForRecall, intentForChronicle)
    }
}

private class NormalClaim(
    speaker: Player,
    context: DiscussionContext,
    statement: Statement,
    intentForRecall: String,
    intentForChronicle: String,
) : Claim(speaker, context, statement, intentForRecall, intentForChronicle)

class FallbackClaim(speaker: Player, context: DiscussionContext) : Claim(
    speaker, context, Statement.Plain(""),
    intentForRecall = "回答取得に失敗したため空文字を返却",
    intentForChronicle = "回答取得に失敗したため空文字を返却",
)
