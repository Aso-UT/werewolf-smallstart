package werewolf.lodge

import werewolf.ai.LanguageModel
import werewolf.ai.poc.PocConsoleLanguageModel

class PocAiLodge(humanConnection: HumanConnection) : AiLodge(humanConnection) {
    override fun createLanguageModel(): LanguageModel = PocConsoleLanguageModel()
}
