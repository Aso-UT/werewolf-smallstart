package werewolf.game

sealed class Choice(
    val chooser: Player,
    val context: SelectionContext,
    val selected: Player,
    private val intentForRecall: String,
    override val intentForChronicle: String,
) : Recallable() {
    init {
        require(selected in context.candidates()) { "${selected.name} is not in candidates" }
    }

    override fun recall() = "<${context.title}> ${selected.name} <$intentForRecall>"
    override fun chronicle() = "[${chooser.name}] [${context.title}] ${selected.name}"

    companion object {
        operator fun invoke(
            chooser: Player,
            context: SelectionContext,
            selected: Player,
            intent: String,
        ): Choice = NormalChoice(chooser, context, selected, intent, intent)

        operator fun invoke(
            chooser: Player,
            context: SelectionContext,
            selected: Player,
            intentForRecall: String,
            intentForChronicle: String,
        ): Choice = NormalChoice(chooser, context, selected, intentForRecall, intentForChronicle)
    }
}

private class NormalChoice(
    chooser: Player,
    context: SelectionContext,
    selected: Player,
    intentForRecall: String,
    intentForChronicle: String,
) : Choice(chooser, context, selected, intentForRecall, intentForChronicle)

class FallbackChoice(chooser: Player, context: SelectionContext) : Choice(
    chooser, context, context.candidates().random(),
    intentForRecall = "回答取得に失敗したためランダム選択",
    intentForChronicle = "回答取得に失敗したためランダム選択",
)
