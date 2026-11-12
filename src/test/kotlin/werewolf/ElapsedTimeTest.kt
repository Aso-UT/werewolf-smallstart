package werewolf

import werewolf.game.toMinutesSecondsString

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ElapsedTimeTest {

    @Test
    fun `shows minutes and zero-padded seconds`() {
        assertEquals("1分05秒", (1.minutes + 5.seconds).toMinutesSecondsString())
    }

    @Test
    fun `shows only seconds when shorter than a minute`() {
        assertEquals("45秒", 45.seconds.toMinutesSecondsString())
    }

    @Test
    fun `drops fractions of a second`() {
        assertEquals("3分12秒", (3.minutes + 12.seconds + 999.milliseconds).toMinutesSecondsString())
    }
}
