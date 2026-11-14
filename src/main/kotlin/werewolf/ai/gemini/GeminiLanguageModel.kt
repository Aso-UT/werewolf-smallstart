package werewolf.ai.gemini

import com.google.genai.Client
import com.google.genai.types.Content
import com.google.genai.types.GenerateContentConfig
import com.google.genai.types.Part
import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.RuminationRecord
import kotlin.time.measureTimedValue

class GeminiLanguageModel(
    private val model: String = DEFAULT_MODEL,
) : LanguageModel {
    private val client = Client()

    override fun ask(system: String, history: List<String>, instruction: String): Completion {
        val user = buildString {
            if (history.isNotEmpty()) {
                appendLine("【ここまでのゲームの流れ】")
                history.forEach { appendLine(it) }
                appendLine()
            }
            append(instruction)
        }
        val config = GenerateContentConfig.builder()
            .systemInstruction(Content.fromParts(Part.fromText(system)))
            .build()
        // Catch broadly and swallow cause to prevent API key leakage via exception messages or URLs
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            val (response, responseTime) = measureTimedValue { client.models.generateContent(model, user, config) }
            val usageMeta = response.usageMetadata().orElse(null)
            val metadata = GeminiMetadata(
                model = model,
                promptTokenCount = usageMeta?.promptTokenCount()?.orElse(null),
                candidatesTokenCount = usageMeta?.candidatesTokenCount()?.orElse(null),
                responseTime = responseTime,
            )
            Completion.Answer(response.text() ?: "", metadata)
        } catch (e: Exception) {
            throw GeminiApiException("Gemini API call failed: ${e.javaClass.simpleName}")
        }
    }

    // Gemini ではプロンプトキャッシュを使っていないので、反芻はしない
    override fun drainRuminations(): List<RuminationRecord> = emptyList()
    override fun stop() { /* no ruminations to stop */ }

    class GeminiApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "gemini-2.5-flash"
    }
}
