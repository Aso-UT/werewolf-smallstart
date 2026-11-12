package werewolf

import werewolf.ai.anthropic.AnthropicEffort
import werewolf.ai.anthropic.AnthropicMetadata
import werewolf.ai.anthropic.AnthropicSummableMetadata
import werewolf.ai.anthropic.CacheDiagnostics
import werewolf.ai.anthropic.TokenUsage
import werewolf.game.ResponseTimeStats
import werewolf.game.UnimplementedSummableMetadata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class AnthropicMetadataTest {

    private fun metadata(thinkingSummary: String) = AnthropicMetadata(
        model = "test-model",
        tokenUsage = TokenUsage(input = 1, output = 2, cacheCreationInput = 0, cacheReadInput = 0, thinking = 0),
        cacheDiagnostics = CacheDiagnostics(cachedItemCount = 0, newItemCount = 1, timeSinceLastCallMs = null),
        stopReason = "end_turn",
        effort = AnthropicEffort.HIGH,
        thinkingSummary = thinkingSummary,
        responseTime = 2340.milliseconds,
    )

    @Test
    fun `response time is shown in seconds`() {
        val display = metadata("").toDisplayString()

        assertTrue(display.contains(" response=2.3s "))
    }

    @Test
    fun `thinking summary is appended after stats`() {
        val display = metadata("Wolfが怪しい").toDisplayString()

        assertTrue(display.startsWith("model=test-model"))
        assertTrue(display.endsWith(" | 思考要約: Wolfが怪しい"))
    }

    @Test
    fun `newlines in thinking summary are collapsed into single spaces`() {
        val display = metadata("Wolfが怪しい。\n\n投票先は\nWolfにする").toDisplayString()

        assertTrue(display.endsWith(" | 思考要約: Wolfが怪しい。 投票先は Wolfにする"))
        assertFalse(display.contains("\n"))
    }

    @Test
    fun `empty thinking summary is omitted`() {
        val display = metadata("").toDisplayString()

        assertFalse(display.contains("思考要約"))
    }

    @Test
    fun `blank thinking summary is omitted`() {
        val display = metadata(" \n ").toDisplayString()

        assertFalse(display.contains("思考要約"))
    }

    private fun summable(model: String = "test-model", effort: AnthropicEffort = AnthropicEffort.HIGH, input: Long = 0) =
        AnthropicSummableMetadata(
            model = model,
            effort = effort,
            tokenUsage = TokenUsage(input = input, output = 0, cacheCreationInput = 0, cacheReadInput = 0, thinking = 0),
            responseTime = ResponseTimeStats(1.seconds),
            count = 1,
        )

    @Test
    fun `summable has model and effort in decision method and starts with count 1`() {
        val summable = metadata("").toSummable()

        assertEquals("Anthropic test-model effort=HIGH", summable.decisionMethod)
        assertEquals(1, summable.count)
    }

    @Test
    fun `plus adds token usages, response times and counts`() {
        val a = AnthropicSummableMetadata(
            "m", AnthropicEffort.HIGH,
            TokenUsage(input = 1, output = 2, cacheCreationInput = 3, cacheReadInput = 4, thinking = 5),
            ResponseTimeStats(total = 3.seconds, max = 2.seconds), count = 2,
        )
        val b = AnthropicSummableMetadata(
            "m", AnthropicEffort.HIGH,
            TokenUsage(input = 10, output = 20, cacheCreationInput = 30, cacheReadInput = 40, thinking = 50),
            ResponseTimeStats(total = 6.seconds, max = 4.seconds), count = 3,
        )

        assertEquals(
            AnthropicSummableMetadata(
                "m", AnthropicEffort.HIGH,
                TokenUsage(input = 11, output = 22, cacheCreationInput = 33, cacheReadInput = 44, thinking = 55),
                ResponseTimeStats(total = 9.seconds, max = 4.seconds), count = 5,
            ),
            a + b,
        )
    }

    @Test
    fun `plus rejects different effort`() {
        assertFailsWith<IllegalArgumentException> { summable(effort = AnthropicEffort.HIGH) + summable(effort = AnthropicEffort.LOW) }
    }

    @Test
    fun `plus rejects different model`() {
        assertFailsWith<IllegalArgumentException> { summable(model = "a") + summable(model = "b") }
    }

    @Test
    fun `plus rejects other kinds of summable`() {
        assertFailsWith<IllegalArgumentException> { summable() + UnimplementedSummableMetadata(count = 1) }
    }

    @Test
    fun `summable display shows tokens with thinking as part of output, followed by response times`() {
        val summable = AnthropicSummableMetadata(
            "m", AnthropicEffort.HIGH,
            TokenUsage(input = 12345, output = 1200, cacheCreationInput = 0, cacheReadInput = 10000, thinking = 800),
            ResponseTimeStats(total = 6.seconds, max = 4.seconds), count = 2,
        )

        assertEquals(
            "input 12,345 / output 1,200（うち thinking 800）/ cache create 0 / cache read 10,000" +
                " / response total 6.0s / avg 3.0s / max 4.0s",
            summable.toDisplayString(),
        )
    }
}
