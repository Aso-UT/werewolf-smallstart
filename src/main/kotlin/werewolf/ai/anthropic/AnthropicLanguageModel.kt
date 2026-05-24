package werewolf.ai.anthropic

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.CacheControlEphemeral
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.TextBlockParam
import werewolf.ai.Completion
import werewolf.ai.LanguageModel

class AnthropicLanguageModel(
    private val model: String = DEFAULT_MODEL,
) : LanguageModel {
    private val client: AnthropicClient = AnthropicOkHttpClient.fromEnv()

    override fun ask(system: String, user: String): Completion {
        val params = MessageCreateParams.builder()
            .model(model)
            .maxTokens(MAX_TOKENS)
            .systemOfTextBlockParams(listOf(
                TextBlockParam.builder()
                    .text(system)
                    .cacheControl(CacheControlEphemeral.builder().build())
                    .build()
            ))
            .addUserMessage(user)
            .build()
        // Catch broadly and swallow cause to prevent API key leakage via exception messages or URLs
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            val message = client.messages().create(params)
            val usage = message.usage()
            val metadata = AnthropicMetadata(
                model = model,
                inputTokens = usage.inputTokens(),
                outputTokens = usage.outputTokens(),
                cacheCreationInputTokens = usage.cacheCreationInputTokens().orElse(0L),
                cacheReadInputTokens = usage.cacheReadInputTokens().orElse(0L),
            )
            val text = message.content()
                .mapNotNull { it.text().orElse(null) }
                .joinToString("") { it.text() }
            Completion(text, metadata)
        } catch (e: Exception) {
            throw AnthropicApiException("Anthropic API call failed: ${e.javaClass.simpleName}")
        }
    }

    class AnthropicApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "claude-sonnet-4-6"
        private const val MAX_TOKENS = 16000L
    }
}
