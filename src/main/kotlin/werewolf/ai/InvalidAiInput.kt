package werewolf.ai

import werewolf.game.ChronicleView
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.RecallView

class InvalidAiInput(
    private val actor: Player,
    private val rawResponse: String,
    private val metadata: ModelMetadata,
) : Recallable() {
    override fun toRecallView() = error("InvalidAiInput is not stored in AI memory and should not appear in prompts")
    override fun toChronicleView() = ChronicleView.Action(actor.name, "無効な応答", rawResponse, metadata.toDisplayString())
}
