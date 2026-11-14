package werewolf.ai.anthropic

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

// 専用のスレッドで、一定の間隔ごとに action を呼び続ける。
// 前の action が終わってから次の間隔を数えるので、action が重なって呼ばれることはない。
// action が例外を投げると、以降は呼ばれなくなる。
// スレッドはデーモンなので、stop しなくてもプログラムの終了を妨げない
class Ticker(name: String, interval: Duration, action: () -> Unit) {
    private val executor = Executors.newSingleThreadScheduledExecutor { task ->
        Thread(task, name).apply { isDaemon = true }
    }

    init {
        val millis = interval.inWholeMilliseconds
        executor.scheduleWithFixedDelay(action, millis, millis, TimeUnit.MILLISECONDS)
    }

    // 以降の action を呼ばないようにする。呼んでいる途中なら、終わるまで待ってから戻る。2回目以降は何もしない
    fun stop() {
        executor.shutdown()
        executor.awaitTermination(STOP_TIMEOUT.inWholeMilliseconds, TimeUnit.MILLISECONDS)
    }

    companion object {
        private val STOP_TIMEOUT = 60.seconds
    }
}
