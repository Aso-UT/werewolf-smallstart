package werewolf.lodge

import werewolf.ai.LanguageModel
import werewolf.ai.anthropic.AnthropicEffort
import werewolf.ai.anthropic.AnthropicLanguageModel

class AnthropicLodge(
    humanConnection: HumanConnection,
    private val model: String,
    private val effort: AnthropicEffort,
) : AiLodge(humanConnection) {
    override fun createLanguageModel(): LanguageModel = AnthropicLanguageModel(model, effort)

    companion object {
        const val HAIKU_MODEL = "claude-haiku-4-5-20251001"
        const val SONNET_MODEL = "claude-sonnet-5"
        const val OPUS_MODEL = "claude-opus-4-8"
    }
}
