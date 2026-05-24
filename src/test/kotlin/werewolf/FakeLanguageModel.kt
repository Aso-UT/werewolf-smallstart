package werewolf

import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.ModelMetadata

class FakeLanguageModel(
    vararg responses: String,
    private val metadata: ModelMetadata = ModelMetadata { "" },
) : LanguageModel {
    private val responseQueue = ArrayDeque(responses.toList())
    val prompts = mutableListOf<String>()

    override fun ask(system: String, user: String): Completion {
        prompts.add(user)
        return Completion(responseQueue.removeFirst(), metadata)
    }
}
