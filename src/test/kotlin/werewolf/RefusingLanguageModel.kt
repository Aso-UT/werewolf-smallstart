package werewolf

import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.RuminationRecord
import werewolf.game.ActionMetadata

// 何を聞かれても、partialText まで出力したところで拒否するモデル
class RefusingLanguageModel(
    private val partialText: String,
    private val metadata: ActionMetadata = FakeActionMetadata(),
) : LanguageModel {
    var askCount = 0
        private set

    override fun ask(system: String, history: List<String>, instruction: String): Completion {
        askCount++
        return Completion.Refusal(partialText, metadata)
    }

    override fun drainRuminations(): List<RuminationRecord> = emptyList()

    override fun stop() = Unit
}
