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
    override fun createUrgeLanguageModel(): LanguageModel = AnthropicLanguageModel(HAIKU_MODEL, AnthropicEffort.LOW)

    companion object {
        const val HAIKU_MODEL = "claude-haiku-5-5"
        const val SONNET_MODEL = "claude-sonnet-5-5"
        const val OPUS_MODEL = "claude-opus-5-5"
    }
}
