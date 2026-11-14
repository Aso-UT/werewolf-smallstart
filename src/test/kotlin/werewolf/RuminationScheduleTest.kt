package werewolf

import werewolf.ai.anthropic.RuminationSchedule
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

class RuminationScheduleTest {
    private val time = TestTimeSource()
    private val schedule = RuminationSchedule(time)

    @Test
    fun `is not due before the first ask`() {
        time += 10.minutes
        assertFalse(schedule.isDue())
    }

    @Test
    fun `is not due until the interval has passed since the ask started`() {
        schedule.recordAsk()
        time += RuminationSchedule.INTERVAL - 1.seconds
        assertFalse(schedule.isDue())
    }

    @Test
    fun `is due once the interval has passed since the ask started`() {
        schedule.recordAsk()
        time += RuminationSchedule.INTERVAL
        assertTrue(schedule.isDue())
    }

    @Test
    fun `is not due right after a rumination is recorded`() {
        schedule.recordAsk()
        time += RuminationSchedule.INTERVAL
        schedule.recordRumination()
        assertFalse(schedule.isDue())
    }

    @Test
    fun `next rumination waits for the interval since the previous rumination started`() {
        schedule.recordAsk()
        time += RuminationSchedule.INTERVAL
        schedule.recordRumination()
        time += RuminationSchedule.INTERVAL - 1.seconds
        assertFalse(schedule.isDue())
        time += 1.seconds
        assertTrue(schedule.isDue())
    }

    @Test
    fun `a new ask restarts the interval`() {
        schedule.recordAsk()
        time += RuminationSchedule.INTERVAL - 1.seconds
        schedule.recordAsk()
        time += 1.seconds
        assertFalse(schedule.isDue())
    }

    @Test
    fun `gives up once too long has passed since the last ask`() {
        schedule.recordAsk()
        time += RuminationSchedule.GIVE_UP_AFTER
        assertFalse(schedule.isDue())
    }

    @Test
    fun `is still due just before giving up`() {
        schedule.recordAsk()
        time += RuminationSchedule.GIVE_UP_AFTER - 1.seconds
        assertTrue(schedule.isDue())
    }
}
