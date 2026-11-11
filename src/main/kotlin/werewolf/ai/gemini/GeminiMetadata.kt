package werewolf.ai.gemini

import werewolf.game.ActionMetadata
import werewolf.game.SummableMetadata
import java.util.Locale

class GeminiMetadata(
    val model: String,
    val promptTokenCount: Int?,
    val candidatesTokenCount: Int?,
) : ActionMetadata {
    override fun toDisplayString() = buildString {
        append("model=$model")
        if (promptTokenCount != null) append(" in=$promptTokenCount")
        if (candidatesTokenCount != null) append(" out=$candidatesTokenCount")
    }

    override fun toSummable() = GeminiSummableMetadata(
        model = model,
        input = promptTokenCount?.toLong() ?: 0L,
        output = candidatesTokenCount?.toLong() ?: 0L,
        count = 1,
    )
}

data class GeminiSummableMetadata(
    val model: String,
    val input: Long,
    val output: Long,
    override val count: Int,
) : SummableMetadata {
    override val decisionMethod = "Gemini $model"

    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is GeminiSummableMetadata && other.decisionMethod == decisionMethod) {
            "Cannot sum $decisionMethod with ${other.decisionMethod}"
        }
        return copy(input = input + other.input, output = output + other.output, count = count + other.count)
    }

    override fun toDisplayString() = "input ${input.grouped()} / output ${output.grouped()}"

    private fun Long.grouped(): String = String.format(Locale.ROOT, "%,d", this)
}
