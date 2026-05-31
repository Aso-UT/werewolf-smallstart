package werewolf.game

sealed class Claim(
    val speaker: Player,
    val context: DiscussionContext,
    val statement: Statement,
    private val intentForRecall: String,
    override val intentForChronicle: String,
) : Recallable() {
    init {
        require(statement.type in context.availableTypes) { "${statement.type} is not available in ${context.title}" }
    }

    override fun recall() = "<${context.title}> ${statement.text()} <$intentForRecall>"
    override fun chronicle() = "[${speaker.name}] [${context.title}] ${statement.text()}"

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
    intentForRecall = "",
    intentForChronicle = "回答取得に失敗したため空文字を返却",
)
