package werewolf.ai.anthropic

import com.anthropic.models.messages.RefusalStopDetails
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

// 拒否されたときにAPIが返す stop_details。原因を切り分けるために記録する。
// explanation は人間向けの説明で、文面が変わりうるため表示にだけ使い、解析には使わない
data class RefusalDetails(
    // cyber / bio / frontier_llm / reasoning_extraction / general_harms のいずれか。分類されないこともある
    val category: String?,
    val explanation: String?,
) {
    constructor(details: RefusalStopDetails) : this(
        category = details.category().map { it.asString() }.orElse(null),
        explanation = details.explanation().orElse(null),
    )
}

@Suppress("LongParameterList")
class AnthropicMetadata(
    val model: String,
    val tokenUsage: TokenUsage,
    val cacheDiagnostics: CacheDiagnostics,
    val stopReason: String,
    // 拒否されたときだけある
    val refusal: RefusalDetails?,
    val effort: AnthropicEffort,
    val maxTokens: Long,
    val thinkingSummary: String,
    val responseTime: Duration,
) : ActionMetadata {
    override fun toDisplayString(): String {
        val sinceLast = cacheDiagnostics.timeSinceLastCallMs?.let { "since_last=${it / MS_PER_SECOND}s" } ?: "since_last=-"
        val category = refusal?.let { " category=${it.category ?: "-"}" }.orEmpty()
        val stats = "model=$model in=${tokenUsage.input} out=${tokenUsage.output} thinking=${tokenUsage.thinking}" +
            " cache_create=${tokenUsage.cacheCreationInput} cache_read=${tokenUsage.cacheReadInput}" +
            " cached=${cacheDiagnostics.cachedItemCount} new=${cacheDiagnostics.newItemCount} $sinceLast" +
            " response=${responseTime.toSecondsString()} stop=$stopReason$category effort=$effort max_tokens=$maxTokens"
        val explanation = refusal?.explanation?.let(::singleLine)?.takeIf { it.isNotEmpty() }
        val summary = singleLine(thinkingSummary).takeIf { it.isNotEmpty() }
        return listOfNotNull(stats, explanation?.let { "拒否の説明: $it" }, summary?.let { "思考要約: $it" }).joinToString(" | ")
    }

    override fun toSummable() = AnthropicSummableMetadata(model, effort, maxTokens, tokenUsage, ResponseTimeStats(responseTime), count = 1)

    // コンソールの「[...]」表示が崩れないよう、改行などの連続する空白を1つの空白にまとめる
    private fun singleLine(text: String): String = text.trim().replace(WHITESPACE, " ")

    companion object {
        private const val MS_PER_SECOND = 1000
        private val WHITESPACE = Regex("\\s+")
    }
}

// effort によってトークン数が大きく変わるため、model と effort の組み合わせを判断方式とする。
// max_tokens も含めるのは、キャッシュを保つだけの反芻（max_tokens=0）を本番の呼び出しと分けて合算するため
data class AnthropicSummableMetadata(
    val model: String,
    val effort: AnthropicEffort,
    val maxTokens: Long,
    val tokenUsage: TokenUsage,
    val responseTime: ResponseTimeStats,
    override val count: Int,
) : SummableMetadata {
    override val decisionMethod = "Anthropic $model effort=$effort max_tokens=$maxTokens"

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

// 呼び出しに失敗したとき。例外のメッセージには API キーなどが含まれうるので、例外の種類だけを残す
class AnthropicFailureMetadata(
    val model: String,
    val effort: AnthropicEffort,
    val maxTokens: Long,
    val error: String,
) : ActionMetadata {
    override fun toDisplayString() = "model=$model effort=$effort max_tokens=$maxTokens failed=$error"

    override fun toSummable() = AnthropicFailureSummableMetadata(model, effort, maxTokens, count = 1)
}

// 失敗した呼び出しはトークン数などを持たないので、成功した呼び出しとは分けて件数だけを合算する
data class AnthropicFailureSummableMetadata(
    val model: String,
    val effort: AnthropicEffort,
    val maxTokens: Long,
    override val count: Int,
) : SummableMetadata {
    override val decisionMethod = "Anthropic $model effort=$effort max_tokens=$maxTokens 失敗"

    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is AnthropicFailureSummableMetadata && other.decisionMethod == decisionMethod) {
            "Cannot sum $decisionMethod with ${other.decisionMethod}"
        }
        return copy(count = count + other.count)
    }

    override fun toDisplayString() = ""
}
