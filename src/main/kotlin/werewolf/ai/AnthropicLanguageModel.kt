package werewolf.ai

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.CacheControlEphemeral
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.TextBlockParam

class AnthropicLanguageModel(
    private val model: String = DEFAULT_MODEL,
) : LanguageModel {
    private val client: AnthropicClient = AnthropicOkHttpClient.fromEnv()

    override fun ask(system: String, user: String): String {
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
            client.messages().create(params).content()
                .mapNotNull { it.text().orElse(null) }
                .joinToString("") { it.text() }
        } catch (e: Exception) {
            throw AnthropicApiException("Anthropic API call failed: ${e.javaClass.simpleName}")
        }
    }

    class AnthropicApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "claude-haiku-4-5"
        private const val MAX_TOKENS = 16000L
    }
}
