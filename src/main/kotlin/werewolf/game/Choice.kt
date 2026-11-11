package werewolf.game

sealed class Choice(
    val chooser: Player,
    val context: SelectionContext,
    val selected: Player,
    private val intent: String,
    override val metadata: ActionMetadata,
) : Recallable(), PlayerAction {
    init {
        require(selected in context.candidates()) { "${selected.name} is not in candidates" }
    }

    override fun toRecallView() = RecallView.SelfAction(context.title, selected.name, intent)
    override fun toChronicleView() = ChronicleView.Action(chooser.name, context.title, selected.name, intentWithMetadata(intent))

    companion object {
        operator fun invoke(
            chooser: Player,
            context: SelectionContext,
            selected: Player,
            intent: String,
            metadata: ActionMetadata,
        ): Choice = NormalChoice(chooser, context, selected, intent, metadata)
    }
}

private class NormalChoice(
    chooser: Player,
    context: SelectionContext,
    selected: Player,
    intent: String,
    metadata: ActionMetadata,
) : Choice(chooser, context, selected, intent, metadata)

class FallbackChoice(chooser: Player, context: SelectionContext) : Choice(
    chooser, context, context.candidates().random(),
    intent = "回答取得に失敗したためランダム選択",
    metadata = FallbackActionMetadata,
)
