package werewolf.ai.anthropic

import com.anthropic.client.AnthropicClient
import com.anthropic.client.okhttp.AnthropicOkHttpClient
import com.anthropic.models.messages.CacheControlEphemeral
import com.anthropic.models.messages.ContentBlockParam
import com.anthropic.models.messages.Message
import com.anthropic.models.messages.MessageCreateParams
import com.anthropic.models.messages.OutputConfig
import com.anthropic.models.messages.TextBlockParam
import com.anthropic.models.messages.ThinkingConfigAdaptive
import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.RuminationRecord
import werewolf.game.Moment
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.measureTimedValue

class AnthropicLanguageModel(
    private val model: String = DEFAULT_MODEL,
    private val effort: AnthropicEffort = AnthropicEffort.HIGH,
) : LanguageModel {
    private val client: AnthropicClient = AnthropicOkHttpClient.fromEnv()
    private val blockManager = HistoryBlockManager()
    @Volatile private var lastCallTime: Long? = null

    @Volatile private var lastRequest: LastRequest? = null
    private val schedule = RuminationSchedule()
    private val ruminations = ConcurrentLinkedQueue<RuminationRecord>()
    private val ticker = Ticker("rumination-$model", CHECK_INTERVAL, ::ruminateIfDue)

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
            .thinking(
                ThinkingConfigAdaptive.builder()
                    .display(ThinkingConfigAdaptive.Display.SUMMARIZED)
                    .build(),
            )
            .outputConfig(OutputConfig.builder().effort(effort.toSdkEffort()).build())
            .systemOfTextBlockParams(listOf(TextBlockParam.builder().text(system).build()))
            .addUserMessageOfBlockParams(buildUserBlocks(blocks, instruction))
            .build()
        lastRequest = LastRequest(params, blockManager.committedItemCount)
        schedule.recordAsk()
        // Catch broadly and swallow cause to prevent API key leakage via exception messages or URLs
        @Suppress("TooGenericExceptionCaught", "SwallowedException")
        return try {
            val (message, responseTime) = measureTimedValue { client.messages().create(params) }
            lastCallTime = System.currentTimeMillis()
            val text = message.content()
                .mapNotNull { it.text().orElse(null) }
                .joinToString("") { it.text() }
            Completion(text, buildMetadata(message, diagnostics, MAX_TOKENS, responseTime))
        } catch (e: Exception) {
            throw AnthropicApiException("Anthropic API call failed: ${e.javaClass.simpleName}")
        }
    }

    override fun drainRuminations(): List<RuminationRecord> = generateSequence { ruminations.poll() }.toList()

    override fun stop() = ticker.stop()

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun ruminateIfDue() {
        val request = lastRequest ?: return
        if (!schedule.isDue()) return
        schedule.recordRumination()
        val occurredAt = Moment.now()
        val diagnostics = CacheDiagnostics(
            cachedItemCount = request.itemCount,
            newItemCount = 0,
            timeSinceLastCallMs = lastCallTime?.let { System.currentTimeMillis() - it },
        )
        try {
            // 前回の本番のリクエストをそのまま送り直す。キャッシュの区切り位置までが本番と完全に一致するようにするため
            val params = request.params.toBuilder().maxTokens(RUMINATION_MAX_TOKENS).build()
            val (message, responseTime) = measureTimedValue { client.messages().create(params) }
            ruminations.add(RuminationRecord(occurredAt, buildMetadata(message, diagnostics, RUMINATION_MAX_TOKENS, responseTime)))
        } catch (e: Exception) {
            // 反芻に失敗しても本番の呼び出しには影響しないので、記録せずに次の機会を待つ
        }
    }

    private fun buildMetadata(message: Message, diagnostics: CacheDiagnostics, maxTokens: Long, responseTime: Duration) =
        AnthropicMetadata(
            model = model,
            tokenUsage = TokenUsage(message.usage()),
            cacheDiagnostics = diagnostics,
            stopReason = message.stopReason().map { it.asString() }.orElse("unknown"),
            effort = effort,
            maxTokens = maxTokens,
            thinkingSummary = extractThinkingSummary(message),
            responseTime = responseTime,
        )

    // display: SUMMARIZED を指定しているため、thinkingブロックには思考の要約が入る
    private fun extractThinkingSummary(message: Message): String = message.content()
        .mapNotNull { it.thinking().orElse(null) }
        .joinToString("\n") { it.thinking() }

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

    private class LastRequest(val params: MessageCreateParams, val itemCount: Int)

    class AnthropicApiException(message: String) : Exception(message)

    companion object {
        private const val DEFAULT_MODEL = "claude-opus-5-5"
        private const val MAX_TOKENS = 16000L
        // max_tokens: 0 なら、キャッシュの読み書きだけをして、出力を生成せずに返る
        private const val RUMINATION_MAX_TOKENS = 0L
        private val CHECK_INTERVAL = 10.seconds
    }
}
