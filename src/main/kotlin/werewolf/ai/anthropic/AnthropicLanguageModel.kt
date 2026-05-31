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
    private var cachedCount = 0
    private var lastCallTime: Long = -1L

    override fun ask(system: String, history: List<String>, instruction: String): Completion {
        val callStartTime = System.currentTimeMillis()
        val diagnostics = CacheDiagnostics(
            cachedItemCount = cachedCount,
            newItemCount = history.size - cachedCount,
            timeSinceLastCallMs = if (lastCallTime < 0L) null else callStartTime - lastCallTime,
        )
        val params = MessageCreateParams.builder()
            .model(model)
            .maxTokens(MAX_TOKENS)
            .systemOfTextBlockParams(listOf(
                TextBlockParam.builder()
                    .text(system)
                    .cacheControl(CacheControlEphemeral.builder().build())
                    .build()
            ))
            .addUserMessageOfBlockParams(buildUserBlocks(history, instruction))
            .build()
        // Catch broadly and swallow cause to prevent API key leakage via exception messages or URLs
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            val message = client.messages().create(params)
            cachedCount = history.size
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

    private fun buildUserBlocks(history: List<String>, instruction: String): List<ContentBlockParam> = buildList {
        // history[0..cachedCount): キャッシュ済みの範囲。cache_read を狙う
        if (cachedCount > 0) {
            val cachedText = buildString {
                appendLine("【ここまでのゲームの流れ】")
                history.take(cachedCount).forEach { appendLine(it) }
            }.trimEnd()
            add(ContentBlockParam.ofText(
                TextBlockParam.builder()
                    .text(cachedText)
                    .cacheControl(CacheControlEphemeral.builder().build())
                    .build()
            ))
        }
        // history[cachedCount..]: 今回の呼び出しで新たに追加された分。
        // キャッシュマーカーを付けることで、次回呼び出しの cache_read に備える
        val newItems = history.drop(cachedCount)
        if (newItems.isNotEmpty()) {
            val newText = buildString {
                if (cachedCount == 0) appendLine("【ここまでのゲームの流れ】")
                newItems.forEach { appendLine(it) }
            }.trimEnd()
            add(ContentBlockParam.ofText(
                TextBlockParam.builder()
                    .text(newText)
                    .cacheControl(CacheControlEphemeral.builder().build())
                    .build()
            ))
        }
        // instruction: 毎回変わるためキャッシュしない
        add(ContentBlockParam.ofText(
            TextBlockParam.builder()
                .text(instruction)
                .build()
        ))
    }

    class AnthropicApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "claude-sonnet-4-6"
        private const val MAX_TOKENS = 512L
    }
}
