package werewolf.game

import java.util.Locale
import kotlin.time.Duration
import kotlin.time.DurationUnit

// 行動を決めるのにかかった時間を、件数をまたいで合算したもの。平均は件数で割って出すため、件数は合算する側が持つ
data class ResponseTimeStats(val total: Duration, val max: Duration) {
    constructor(single: Duration) : this(total = single, max = single)

    operator fun plus(other: ResponseTimeStats) = ResponseTimeStats(total + other.total, maxOf(max, other.max))

    fun toDisplayString(count: Int): String =
        "response total ${total.toSecondsString()} / avg ${(total / count).toSecondsString()} / max ${max.toSecondsString()}"
}

fun Duration.toSecondsString(): String = String.format(Locale.ROOT, "%.1fs", toDouble(DurationUnit.SECONDS))
