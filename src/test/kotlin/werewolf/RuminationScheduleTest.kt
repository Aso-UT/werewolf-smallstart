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
    fun `does not ruminate before the first ask`() {
        time += 10.minutes
        assertFalse(schedule.tryStartRumination())
    }

    @Test
    fun `does not ruminate until the interval has passed since the ask started`() {
        schedule.startAsk()
        time += RuminationSchedule.INTERVAL - 1.seconds
        assertFalse(schedule.tryStartRumination())
    }

    @Test
    fun `ruminates once the interval has passed since the ask started`() {
        schedule.startAsk()
        time += RuminationSchedule.INTERVAL
        assertTrue(schedule.tryStartRumination())
    }

    @Test
    fun `ruminates only once for the same interval`() {
        schedule.startAsk()
        time += RuminationSchedule.INTERVAL
        schedule.tryStartRumination()
        assertFalse(schedule.tryStartRumination())
    }

    @Test
    fun `next rumination waits for the interval since the previous rumination started`() {
        schedule.startAsk()
        time += RuminationSchedule.INTERVAL
        schedule.tryStartRumination()
        time += RuminationSchedule.INTERVAL - 1.seconds
        assertFalse(schedule.tryStartRumination())
        time += 1.seconds
        assertTrue(schedule.tryStartRumination())
    }

    @Test
    fun `a new ask restarts the interval`() {
        schedule.startAsk()
        time += RuminationSchedule.INTERVAL - 1.seconds
        schedule.startAsk()
        time += 1.seconds
        assertFalse(schedule.tryStartRumination())
    }

    @Test
    fun `gives up once too long has passed since the last ask`() {
        schedule.startAsk()
        time += RuminationSchedule.GIVE_UP_AFTER
        assertFalse(schedule.tryStartRumination())
    }

    @Test
    fun `keeps ruminating just before giving up`() {
        schedule.startAsk()
        time += RuminationSchedule.GIVE_UP_AFTER - 1.seconds
        assertTrue(schedule.tryStartRumination())
    }
}
