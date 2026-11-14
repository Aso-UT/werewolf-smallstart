package werewolf

import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.RuminationRecord
import werewolf.game.ActionMetadata

class FakeLanguageModel(
    vararg responses: String,
    private val metadata: ActionMetadata = FakeActionMetadata(),
) : LanguageModel {
    private val responseQueue = ArrayDeque(responses.toList())
    val histories = mutableListOf<List<String>>()
    val instructions = mutableListOf<String>()
    // 次に drainRuminations で渡す反芻。テストから積んでおく
    val pendingRuminations = mutableListOf<RuminationRecord>()
    var stopCount = 0
        private set

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

    override fun drainRuminations(): List<RuminationRecord> = pendingRuminations.toList().also { pendingRuminations.clear() }

    override fun stop() {
        stopCount++
    }
}
