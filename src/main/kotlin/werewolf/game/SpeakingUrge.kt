package werewolf.game

sealed class SpeakingUrge(
    val player: Player,
    val context: DiscussionContext,
    val eagerness: Eagerness,
    private val intentForRecall: String,
    private val intentForChronicle: String,
) : Recallable() {
    private val title get() = "${context.title} - 発言欲求"
    private val content get() = "意欲 ${eagerness.value}"

    override fun toRecallView() = RecallView.SelfAction(title, content, intentForRecall)
    override fun toChronicleView() = ChronicleView.Action(player.name, title, content, intentForChronicle)

    companion object {
        operator fun invoke(
            player: Player,
            context: DiscussionContext,
            eagerness: Eagerness,
            intent: String,
        ): SpeakingUrge = NormalSpeakingUrge(player, context, eagerness, intent, intent)

        operator fun invoke(
            player: Player,
            context: DiscussionContext,
            eagerness: Eagerness,
            intentForRecall: String,
            intentForChronicle: String,
        ): SpeakingUrge = NormalSpeakingUrge(player, context, eagerness, intentForRecall, intentForChronicle)
    }
}

private class NormalSpeakingUrge(
    player: Player,
    context: DiscussionContext,
    eagerness: Eagerness,
    intentForRecall: String,
    intentForChronicle: String,
) : SpeakingUrge(player, context, eagerness, intentForRecall, intentForChronicle)

class FallbackSpeakingUrge(player: Player, context: DiscussionContext) : SpeakingUrge(
    player, context, Eagerness.DEFAULT,
    intentForRecall = "回答取得に失敗したため既定の意欲を返却",
    intentForChronicle = "回答取得に失敗したため既定の意欲を返却",
)
