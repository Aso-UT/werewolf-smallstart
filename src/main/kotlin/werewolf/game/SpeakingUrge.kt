package werewolf.game

sealed class SpeakingUrge(
    val player: Player,
    val context: DiscussionContext,
    val eagerness: Eagerness,
    private val intent: String,
    override val metadata: ActionMetadata,
) : Recallable(), PlayerAction {
    private val title get() = "${context.title} - 発言欲求"
    private val content get() = "意欲 ${eagerness.value}"

    override fun toRecallView() = RecallView.SelfAction(title, content, intent)
    override fun toChronicleView() = ChronicleView.Action(player.name, title, content, intentWithMetadata(intent))

    companion object {
        operator fun invoke(
            player: Player,
            context: DiscussionContext,
            eagerness: Eagerness,
            intent: String,
            metadata: ActionMetadata,
        ): SpeakingUrge = NormalSpeakingUrge(player, context, eagerness, intent, metadata)
    }
}

private class NormalSpeakingUrge(
    player: Player,
    context: DiscussionContext,
    eagerness: Eagerness,
    intent: String,
    metadata: ActionMetadata,
) : SpeakingUrge(player, context, eagerness, intent, metadata)

class FallbackSpeakingUrge(player: Player, context: DiscussionContext) : SpeakingUrge(
    player, context, Eagerness.DEFAULT,
    intent = "回答取得に失敗したため既定の意欲を返却",
    metadata = FallbackActionMetadata,
)
