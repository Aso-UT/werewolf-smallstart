package org.example

sealed class Claim(
    val speaker: Player,
    val context: DiscussionContext,
    val statement: Statement,
    private val intentForRecall: String,
    val intentForChronicle: String,
) : Recallable() {
    init {
        require(statement.type in context.availableTypes) { "${statement.type} is not available in ${context.title}" }
    }

    override fun recall() = "[${context.title}] ${statement.text()} [$intentForRecall]"
    override fun chronicle() = "[${speaker.name}] [${context.title}] ${statement.text()} [$intentForChronicle]"

    companion object {
        operator fun invoke(
            speaker: Player,
            context: DiscussionContext,
            statement: Statement,
            intent: String,
        ): Claim = NormalClaim(speaker, context, statement, intent)
    }
}

private class NormalClaim(
    speaker: Player,
    context: DiscussionContext,
    statement: Statement,
    intent: String,
) : Claim(speaker, context, statement, intent, intent)

class FallbackClaim(speaker: Player, context: DiscussionContext) : Claim(
    speaker, context, Statement.Plain(""),
    intentForRecall = "",
    intentForChronicle = "回答取得に失敗したため空文字を返却",
)
