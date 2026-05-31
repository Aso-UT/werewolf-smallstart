package werewolf.ai.anthropic

import werewolf.ai.ModelMetadata

data class CacheDiagnostics(
    val cachedItemCount: Int,
    val newItemCount: Int,
    val timeSinceLastCallMs: Long?,
)

class AnthropicMetadata(
    val model: String,
    val inputTokens: Long,
    val outputTokens: Long,
    val cacheCreationInputTokens: Long,
    val cacheReadInputTokens: Long,
    val cacheDiagnostics: CacheDiagnostics,
) : ModelMetadata {
    override fun toDisplayString(): String {
        val elapsed = cacheDiagnostics.timeSinceLastCallMs?.let { "elapsed=${it / MS_PER_SECOND}s" } ?: "elapsed=-"
        return "model=$model in=$inputTokens out=$outputTokens" +
            " cache_create=$cacheCreationInputTokens cache_read=$cacheReadInputTokens" +
            " cached=${cacheDiagnostics.cachedItemCount} new=${cacheDiagnostics.newItemCount} $elapsed"
    }

    companion object {
        private const val MS_PER_SECOND = 1000
    }
}
