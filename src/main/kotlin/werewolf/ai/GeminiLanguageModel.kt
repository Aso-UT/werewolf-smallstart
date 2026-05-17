package werewolf.ai

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part

class GeminiLanguageModel(
    private val model: String = DEFAULT_MODEL,
) : LanguageModel {
    private val client = Client()

    override fun ask(system: String, user: String): String {
        val config = GenerateContentConfig.builder()
            .systemInstruction(Content.fromParts(Part.fromText(system)))
            .build()
        // Catch broadly and swallow cause to prevent API key leakage via exception messages or URLs
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            client.models.generateContent(model, user, config).text() ?: ""
        } catch (e: Exception) {
            throw GeminiApiException("Gemini API call failed: ${e.javaClass.simpleName}")
        }
    }

    class GeminiApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
    }
}
