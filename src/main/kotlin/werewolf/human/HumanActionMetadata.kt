package werewolf.human

import werewolf.game.ActionMetadata
import werewolf.game.ResponseTimeStats
import werewolf.game.SummableMetadata
import werewolf.game.toSecondsString
import kotlin.time.Duration

// 制限時間のあるボタンと自由記述では所要時間の性質が違うため、行動の種類ごとに別の判断方式として合算する
enum class HumanActionKind(val displayName: String) {
    SPEAKING_URGE("発言欲求"),
    STATEMENT("発言"),
    CHOICE("選択"),
}

class HumanActionMetadata(private val kind: HumanActionKind, private val responseTime: Duration) : ActionMetadata {
    override fun toDisplayString() = "response=${responseTime.toSecondsString()}"
    override fun toSummable() = HumanSummableMetadata(kind, ResponseTimeStats(responseTime), count = 1)
}

data class HumanSummableMetadata(
    val kind: HumanActionKind,
    val responseTime: ResponseTimeStats,
    override val count: Int,
) : SummableMetadata {
    override val decisionMethod = "人間 ${kind.displayName}"

    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is HumanSummableMetadata && other.decisionMethod == decisionMethod) {
            "Cannot sum $decisionMethod with ${other.decisionMethod}"
        }
        return copy(responseTime = responseTime + other.responseTime, count = count + other.count)
    }

    override fun toDisplayString() = responseTime.toDisplayString(count)
}
