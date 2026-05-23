package werewolf.ai

class AnthropicMetadata(
    val model: String,
    val inputTokens: Long,
    val outputTokens: Long,
    val cacheCreationInputTokens: Long,
    val cacheReadInputTokens: Long,
) : ModelMetadata {
    override fun toDisplayString() =
        "model=$model in=$inputTokens out=$outputTokens cache_create=$cacheCreationInputTokens cache_read=$cacheReadInputTokens"
}
