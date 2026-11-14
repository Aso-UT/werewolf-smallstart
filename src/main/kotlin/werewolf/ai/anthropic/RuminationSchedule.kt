package werewolf.ai.anthropic

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// プロンプトキャッシュのTTL（5分）が切れる前に、反芻を送るかどうかを決める。
// TTL は送り始めた時点から数えるので、本番・反芻とも送り始めた時点を記録する。
// 本番の呼び出しのスレッドが記録した時点を反芻を確かめるタイマーのスレッドから読めるよう、どのメソッドも同期する
class RuminationSchedule(private val timeSource: TimeSource = TimeSource.Monotonic) {
    private var lastSentAt: TimeMark? = null
    private var lastAskedAt: TimeMark? = null

    @Synchronized
    fun recordAsk() {
        val now = timeSource.markNow()
        lastSentAt = now
        lastAskedAt = now
    }

    @Synchronized
    fun recordRumination() {
        lastSentAt = timeSource.markNow()
    }

    @Synchronized
    fun isDue(): Boolean = isCacheExpiring() && !isAbandoned()

    // 最後に送り始めてから、反芻すべき間隔が経った
    private fun isCacheExpiring(): Boolean {
        val sentAt = lastSentAt ?: return false
        return sentAt.elapsedNow() >= INTERVAL
    }

    // 最後の本番から、反芻を打ち切るべき間隔が経った
    private fun isAbandoned(): Boolean {
        val askedAt = lastAskedAt ?: return false
        return askedAt.elapsedNow() >= GIVE_UP_AFTER
    }

    companion object {
        // TTL の5分に、送るまでの遅れと確かめる間隔の分の余裕を持たせる
        val INTERVAL: Duration = 240.seconds
        // 人間が長時間放置したときに、反芻を送り続けないための上限
        val GIVE_UP_AFTER: Duration = 60.minutes
    }
}
