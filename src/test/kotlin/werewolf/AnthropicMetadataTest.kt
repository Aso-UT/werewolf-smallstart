package werewolf

import werewolf.ai.anthropic.AnthropicEffort
import werewolf.ai.anthropic.AnthropicMetadata
import werewolf.ai.anthropic.CacheDiagnostics
import werewolf.ai.anthropic.TokenUsage

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AnthropicMetadataTest {

    private fun metadata(thinkingSummary: String) = AnthropicMetadata(
        model = "test-model",
        tokenUsage = TokenUsage(input = 1, output = 2, cacheCreationInput = 0, cacheReadInput = 0, thinking = 0),
        cacheDiagnostics = CacheDiagnostics(cachedItemCount = 0, newItemCount = 1, timeSinceLastCallMs = null),
        stopReason = "end_turn",
        effort = AnthropicEffort.HIGH,
        thinkingSummary = thinkingSummary,
    )

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
}
