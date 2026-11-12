package werewolf.ai.gemini

import werewolf.game.ActionMetadata
import werewolf.game.ResponseTimeStats
import werewolf.game.SummableMetadata
import werewolf.game.toSecondsString
import java.util.Locale
import kotlin.time.Duration

class GeminiMetadata(
    val model: String,
    val promptTokenCount: Int?,
    val candidatesTokenCount: Int?,
    val responseTime: Duration,
) : ActionMetadata {
    override fun toDisplayString() = buildString {
        append("model=$model")
        if (promptTokenCount != null) append(" in=$promptTokenCount")
        if (candidatesTokenCount != null) append(" out=$candidatesTokenCount")
        append(" response=${responseTime.toSecondsString()}")
    }

    override fun toSummable() = GeminiSummableMetadata(
        model = model,
        input = promptTokenCount?.toLong() ?: 0L,
        output = candidatesTokenCount?.toLong() ?: 0L,
        responseTime = ResponseTimeStats(responseTime),
        count = 1,
    )
}

data class GeminiSummableMetadata(
    val model: String,
    val input: Long,
    val output: Long,
    val responseTime: ResponseTimeStats,
    override val count: Int,
) : SummableMetadata {
    override val decisionMethod = "Gemini $model"

    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is GeminiSummableMetadata && other.decisionMethod == decisionMethod) {
            "Cannot sum $decisionMethod with ${other.decisionMethod}"
        }
        return copy(
            input = input + other.input,
            output = output + other.output,
            responseTime = responseTime + other.responseTime,
            count = count + other.count,
        )
    }

    override fun toDisplayString() =
        "input ${input.grouped()} / output ${output.grouped()} / ${responseTime.toDisplayString(count)}"

    private fun Long.grouped(): String = String.format(Locale.ROOT, "%,d", this)
}
