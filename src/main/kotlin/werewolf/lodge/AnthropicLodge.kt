package werewolf.lodge

import werewolf.ai.LanguageModel
import werewolf.ai.anthropic.AnthropicLanguageModel

class AnthropicLodge(humanConnection: HumanConnection) : AiLodge(humanConnection) {
    override fun createLanguageModel(): LanguageModel = AnthropicLanguageModel(HAIKU_MODEL)

    companion object {
        private const val HAIKU_MODEL = "claude-haiku-4-5-20251001"
    }
}
