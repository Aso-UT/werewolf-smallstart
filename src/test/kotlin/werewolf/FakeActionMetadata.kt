package werewolf

import werewolf.game.ActionMetadata
import werewolf.game.SummableMetadata

class FakeActionMetadata(
    private val display: String = "",
    private val decisionMethod: String = "Fake",
    private val tokens: Long = 0,
) : ActionMetadata {
    override fun toDisplayString() = display
    override fun toSummable() = FakeSummableMetadata(decisionMethod, tokens, count = 1)
}

data class FakeSummableMetadata(
    override val decisionMethod: String,
    val tokens: Long,
    override val count: Int,
) : SummableMetadata {
    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is FakeSummableMetadata && other.decisionMethod == decisionMethod)
        return copy(tokens = tokens + other.tokens, count = count + other.count)
    }

    override fun toDisplayString() = "tokens $tokens"
}
