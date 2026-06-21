package werewolf.game

sealed class Choice(
    val chooser: Player,
    val context: SelectionContext,
    val selected: Player,
    private val intentForRecall: String,
    private val intentForChronicle: String,
) : Recallable() {
    init {
        require(selected in context.candidates()) { "${selected.name} is not in candidates" }
    }

    override fun toRecallView() = RecallView.Action(context.title, selected.name, intentForRecall)
    override fun toChronicleView() = ChronicleView.Action(chooser.name, context.title, selected.name, intentForChronicle)

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
