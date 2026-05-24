package werewolf.ai.gemini

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.ModelMetadata

class GeminiLanguageModel(
    private val model: String = DEFAULT_MODEL,
) : LanguageModel {
    private val client = Client()

    override fun ask(system: String, user: String): Completion {
        val config = GenerateContentConfig.builder()
            .systemInstruction(Content.fromParts(Part.fromText(system)))
            .build()
        // Catch broadly and swallow cause to prevent API key leakage via exception messages or URLs
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            val response = client.models.generateContent(model, user, config)
            val usageMeta = response.usageMetadata().orElse(null)
            val metadata = GeminiMetadata(
                model = model,
                promptTokenCount = usageMeta?.promptTokenCount()?.orElse(null),
                candidatesTokenCount = usageMeta?.candidatesTokenCount()?.orElse(null),
            )
            Completion(response.text() ?: "", metadata)
        } catch (e: Exception) {
            throw GeminiApiException("Gemini API call failed: ${e.javaClass.simpleName}")
        }
    }

    private class GeminiMetadata(
        val model: String,
        val promptTokenCount: Int?,
        val candidatesTokenCount: Int?,
    ) : ModelMetadata {
        override fun toDisplayString() = buildString {
            append("model=$model")
            if (promptTokenCount != null) append(" in=$promptTokenCount")
            if (candidatesTokenCount != null) append(" out=$candidatesTokenCount")
        }
    }

    class GeminiApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
    }
}
