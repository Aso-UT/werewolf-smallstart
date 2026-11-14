package werewolf.ai.anthropic

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

// プロンプトキャッシュのTTL（5分）が切れる前に、反芻を送るかどうかを決める。
// TTL は送り始めた時点から数えるので、本番・反芻とも送り始めた時点を記録する。
// 本番の呼び出しのスレッドと、反芻を確かめるタイマーのスレッドの両方から呼ばれる
class RuminationSchedule(private val timeSource: TimeSource = TimeSource.Monotonic) {
    private var lastSentAt: TimeMark? = null
    private var lastAskedAt: TimeMark? = null

    @Synchronized
    fun startAsk() {
        val now = timeSource.markNow()
        lastSentAt = now
        lastAskedAt = now
    }

    // 反芻すべき時期なら、送り始めたことを記録して true を返す。同時に確かめても、送るのは一度だけになる
    @Synchronized
    fun tryStartRumination(): Boolean {
        if (!isDue()) return false
        lastSentAt = timeSource.markNow()
        return true
    }

    private fun isDue(): Boolean {
        val askedAt = lastAskedAt ?: return false
        val sentAt = lastSentAt ?: return false
        return askedAt.elapsedNow() < GIVE_UP_AFTER && sentAt.elapsedNow() >= INTERVAL
    }

    companion object {
        // TTL の5分に、送るまでの遅れと確かめる間隔の分の余裕を持たせる
        val INTERVAL: Duration = 240.seconds
        // 人間が長時間放置したときに、反芻を送り続けないための上限
        val GIVE_UP_AFTER: Duration = 60.minutes
    }
}
