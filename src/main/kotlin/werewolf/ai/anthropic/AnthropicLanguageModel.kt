package werewolf.ai.anthropic

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.CacheControlEphemeral
import com.anthropic.models.messages.ContentBlockParam
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.TextBlockParam
import werewolf.ai.Completion
import werewolf.ai.LanguageModel

class AnthropicLanguageModel(
    private val model: String = DEFAULT_MODEL,
) : LanguageModel {
    private val client: AnthropicClient = AnthropicOkHttpClient.fromEnv()
    private val blockManager = HistoryBlockManager()
    private var lastCallTime: Long? = null

    override fun ask(system: String, history: List<String>, instruction: String): Completion {
        val callStartTime = System.currentTimeMillis()
        val diagnostics = CacheDiagnostics(
            cachedItemCount = blockManager.committedItemCount,
            newItemCount = history.size - blockManager.committedItemCount,
            timeSinceLastCallMs = lastCallTime?.let { callStartTime - it },
        )
        val blocks = blockManager.buildBlocks(history)
        val params = MessageCreateParams.builder()
            .model(model)
            .maxTokens(MAX_TOKENS)
            .systemOfTextBlockParams(listOf(TextBlockParam.builder().text(system).build()))
            .addUserMessageOfBlockParams(buildUserBlocks(blocks, instruction))
            .build()
        // Catch broadly and swallow cause to prevent API key leakage via exception messages or URLs
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            val message = client.messages().create(params)
            lastCallTime = System.currentTimeMillis()
            val usage = message.usage()
            val metadata = AnthropicMetadata(
                model = model,
                inputTokens = usage.inputTokens(),
                outputTokens = usage.outputTokens(),
                cacheCreationInputTokens = usage.cacheCreationInputTokens().orElse(0L),
                cacheReadInputTokens = usage.cacheReadInputTokens().orElse(0L),
                cacheDiagnostics = diagnostics,
            )
            val text = message.content()
                .mapNotNull { it.text().orElse(null) }
                .joinToString("") { it.text() }
            Completion(text, metadata)
        } catch (e: Exception) {
            throw AnthropicApiException("Anthropic API call failed: ${e.javaClass.simpleName}")
        }
    }

    private fun buildUserBlocks(
        blocks: List<Pair<String, Boolean>>,
        instruction: String,
    ): List<ContentBlockParam> = buildList {
        blocks.forEach { (text, withCache) ->
            val builder = TextBlockParam.builder().text(text)
            if (withCache) builder.cacheControl(CacheControlEphemeral.builder().build())
            add(ContentBlockParam.ofText(builder.build()))
        }
        add(ContentBlockParam.ofText(TextBlockParam.builder().text(instruction).build()))
    }

    class AnthropicApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "claude-sonnet-4-6"
        private const val MAX_TOKENS = 512L
    }
}
