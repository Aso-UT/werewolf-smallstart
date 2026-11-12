package werewolf.ai.anthropic

import com.anthropic.models.messages.Usage
import werewolf.game.ActionMetadata
import werewolf.game.ResponseTimeStats
import werewolf.game.SummableMetadata
import werewolf.game.toSecondsString
import java.util.Locale
import kotlin.time.Duration

data class CacheDiagnostics(
    val cachedItemCount: Int,
    val newItemCount: Int,
    val timeSinceLastCallMs: Long?,
)

data class TokenUsage(
    val input: Long,
    val output: Long,
    val cacheCreationInput: Long,
    val cacheReadInput: Long,
    // output の内数
    val thinking: Long,
) {
    constructor(usage: Usage) : this(
        input = usage.inputTokens(),
        output = usage.outputTokens(),
        cacheCreationInput = usage.cacheCreationInputTokens().orElse(0L),
        cacheReadInput = usage.cacheReadInputTokens().orElse(0L),
        thinking = usage.outputTokensDetails().map { it.thinkingTokens() }.orElse(0L),
    )

    operator fun plus(other: TokenUsage) = TokenUsage(
        input = input + other.input,
        output = output + other.output,
        cacheCreationInput = cacheCreationInput + other.cacheCreationInput,
        cacheReadInput = cacheReadInput + other.cacheReadInput,
        thinking = thinking + other.thinking,
    )

    fun toDisplayString(): String =
        "input ${input.grouped()} / output ${output.grouped()}（うち thinking ${thinking.grouped()}）/" +
            " cache create ${cacheCreationInput.grouped()} / cache read ${cacheReadInput.grouped()}"

    private fun Long.grouped(): String = String.format(Locale.ROOT, "%,d", this)
}

@Suppress("LongParameterList")
class AnthropicMetadata(
    val model: String,
    val tokenUsage: TokenUsage,
    val cacheDiagnostics: CacheDiagnostics,
    val stopReason: String,
    val effort: AnthropicEffort,
    val thinkingSummary: String,
    val responseTime: Duration,
) : ActionMetadata {
    override fun toDisplayString(): String {
        val sinceLast = cacheDiagnostics.timeSinceLastCallMs?.let { "since_last=${it / MS_PER_SECOND}s" } ?: "since_last=-"
        val stats = "model=$model in=${tokenUsage.input} out=${tokenUsage.output} thinking=${tokenUsage.thinking}" +
            " cache_create=${tokenUsage.cacheCreationInput} cache_read=${tokenUsage.cacheReadInput}" +
            " cached=${cacheDiagnostics.cachedItemCount} new=${cacheDiagnostics.newItemCount} $sinceLast" +
            " response=${responseTime.toSecondsString()} stop=$stopReason effort=$effort"
        val summary = singleLineThinkingSummary()
        return if (summary.isEmpty()) stats else "$stats | 思考要約: $summary"
    }

    override fun toSummable() = AnthropicSummableMetadata(model, effort, tokenUsage, ResponseTimeStats(responseTime), count = 1)

    // コンソールの「[...]」表示が崩れないよう、改行などの連続する空白を1つの空白にまとめる
    private fun singleLineThinkingSummary(): String = thinkingSummary.trim().replace(WHITESPACE, " ")

    companion object {
        private const val MS_PER_SECOND = 1000
        private val WHITESPACE = Regex("\\s+")
    }
}

// effort によってトークン数が大きく変わるため、model と effort の組み合わせを判断方式とする
data class AnthropicSummableMetadata(
    val model: String,
    val effort: AnthropicEffort,
    val tokenUsage: TokenUsage,
    val responseTime: ResponseTimeStats,
    override val count: Int,
) : SummableMetadata {
    override val decisionMethod = "Anthropic $model effort=$effort"

    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is AnthropicSummableMetadata && other.decisionMethod == decisionMethod) {
            "Cannot sum $decisionMethod with ${other.decisionMethod}"
        }
        return copy(
            tokenUsage = tokenUsage + other.tokenUsage,
            responseTime = responseTime + other.responseTime,
            count = count + other.count,
        )
    }

    override fun toDisplayString() = "${tokenUsage.toDisplayString()} / ${responseTime.toDisplayString(count)}"
}
