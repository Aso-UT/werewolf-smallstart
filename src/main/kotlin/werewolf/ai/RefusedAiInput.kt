package werewolf.ai

import werewolf.game.ActionMetadata
import werewolf.game.ChronicleView
import werewolf.game.Player
import werewolf.game.PlayerAction
import werewolf.game.Recallable

class RefusedAiInput(
    private val actor: Player,
    private val partialResponse: String,
    override val metadata: ActionMetadata,
) : Recallable(), PlayerAction {
    override fun toRecallView() = error("RefusedAiInput is not stored in AI memory and should not appear in prompts")
    override fun toChronicleView() = ChronicleView.Action(actor.name, "応答拒否", partialResponse, metadata.toDisplayString())
}
