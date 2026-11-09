package werewolf

import werewolf.phase.lastingAtLeast
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

class LastingAtLeastTest {

    @Test
    fun `waits for the duration even when the block finishes immediately`() {
        var executed = false

        val elapsed = measureTime { lastingAtLeast(200.milliseconds) { executed = true } }

        assertTrue(executed)
        assertTrue(elapsed >= 200.milliseconds)
    }
}
