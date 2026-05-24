package werewolf.ai

import werewolf.game.Recallable

class InvalidAiInput(
    private val rawResponse: String,
    private val metadata: ModelMetadata,
) : Recallable() {
    override fun recall() = error("InvalidAiInput is not stored in AI memory and should not appear in prompts")
    override fun chronicle() = "[無効な応答] $rawResponse"
    override val intentForChronicle: String = metadata.toDisplayString()
}
