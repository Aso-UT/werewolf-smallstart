package werewolf

import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.ModelMetadata

class FakeLanguageModel(
    vararg responses: String,
    private val metadata: ModelMetadata = ModelMetadata { "" },
) : LanguageModel {
    private val responseQueue = ArrayDeque(responses.toList())
    val histories = mutableListOf<List<String>>()
    val instructions = mutableListOf<String>()

    /** history と instruction を結合した文字列のリスト。既存テストの assertContains 用。 */
    val prompts: List<String>
        get() = histories.zip(instructions).map { (h, i) ->
            buildString {
                h.forEach { appendLine(it) }
                append(i)
            }
        }

    override fun ask(system: String, history: List<String>, instruction: String): Completion {
        histories.add(history)
        instructions.add(instruction)
        return Completion(responseQueue.removeFirst(), metadata)
    }
}
