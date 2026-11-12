package werewolf

import werewolf.game.ResponseTimeStats

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ResponseTimeStatsTest {

    @Test
    fun `single response time is both total and max`() {
        assertEquals(ResponseTimeStats(total = 2.seconds, max = 2.seconds), ResponseTimeStats(2.seconds))
    }

    @Test
    fun `plus adds totals and keeps the larger max`() {
        val sum = ResponseTimeStats(total = 3.seconds, max = 2.seconds) + ResponseTimeStats(total = 4.seconds, max = 1.seconds)

        assertEquals(ResponseTimeStats(total = 7.seconds, max = 2.seconds), sum)
    }

    @Test
    fun `display shows total, average over count and max in seconds with one decimal`() {
        val stats = ResponseTimeStats(total = 7.seconds, max = 2500.milliseconds)

        assertEquals("response total 7.0s / avg 2.3s / max 2.5s", stats.toDisplayString(count = 3))
    }
}
