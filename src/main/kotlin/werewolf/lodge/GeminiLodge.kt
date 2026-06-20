package werewolf.lodge

import werewolf.ai.LanguageModel
import werewolf.ai.gemini.GeminiLanguageModel

class GeminiLodge(humanConnection: HumanConnection) : AiLodge(humanConnection) {
    override fun createLanguageModel(): LanguageModel = GeminiLanguageModel()
}
