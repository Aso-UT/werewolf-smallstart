package werewolf.ai.anthropic

import com.anthropic.models.messages.Usage
import werewolf.ai.ModelMetadata

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
    val thinking: Long,
) {
    constructor(usage: Usage) : this(
        input = usage.inputTokens(),
        output = usage.outputTokens(),
        cacheCreationInput = usage.cacheCreationInputTokens().orElse(0L),
        cacheReadInput = usage.cacheReadInputTokens().orElse(0L),
        thinking = usage.outputTokensDetails().map { it.thinkingTokens() }.orElse(0L),
    )
}

class AnthropicMetadata(
    val model: String,
    val tokenUsage: TokenUsage,
    val cacheDiagnostics: CacheDiagnostics,
    val stopReason: String,
    val effort: AnthropicEffort,
) : ModelMetadata {
    override fun toDisplayString(): String {
        val elapsed = cacheDiagnostics.timeSinceLastCallMs?.let { "elapsed=${it / MS_PER_SECOND}s" } ?: "elapsed=-"
        return "model=$model in=${tokenUsage.input} out=${tokenUsage.output} thinking=${tokenUsage.thinking}" +
            " cache_create=${tokenUsage.cacheCreationInput} cache_read=${tokenUsage.cacheReadInput}" +
            " cached=${cacheDiagnostics.cachedItemCount} new=${cacheDiagnostics.newItemCount} $elapsed stop=$stopReason effort=$effort"
    }

    companion object {
        private const val MS_PER_SECOND = 1000
    }
}
