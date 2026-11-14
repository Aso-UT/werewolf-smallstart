package werewolf

import werewolf.ai.anthropic.Ticker
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class TickerTest {
    private val tickers = mutableListOf<Ticker>()

    private fun ticker(action: () -> Unit) = Ticker("test-ticker", INTERVAL, action).also { tickers.add(it) }

    @AfterTest
    fun stopAll() = tickers.forEach { it.stop() }

    @Test
    fun `calls the action repeatedly`() {
        val called = CountDownLatch(3)
        ticker { called.countDown() }

        assertTrue(called.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
    }

    @Test
    fun `keeps calling the action after it throws`() {
        val called = CountDownLatch(3)
        ticker {
            called.countDown()
            error("failure in action")
        }

        assertTrue(called.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))
    }

    @Test
    fun `does not call the action after stop returns`() {
        val count = AtomicInteger()
        val called = CountDownLatch(1)
        val ticker = ticker {
            count.incrementAndGet()
            called.countDown()
        }
        assertTrue(called.await(TIMEOUT_SECONDS, TimeUnit.SECONDS))

        ticker.stop()
        val countAtStop = count.get()
        Thread.sleep(INTERVAL.inWholeMilliseconds * 5)

        assertEquals(countAtStop, count.get())
    }

    companion object {
        private val INTERVAL = 10.milliseconds
        private const val TIMEOUT_SECONDS = 5L
    }
}
