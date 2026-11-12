package werewolf

import werewolf.game.ResponseTimeStats
import werewolf.game.UnimplementedSummableMetadata
import werewolf.human.HumanActionKind
import werewolf.human.HumanActionMetadata
import werewolf.human.HumanSummableMetadata

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class HumanActionMetadataTest {

    @Test
    fun `display shows response time in seconds`() {
        assertEquals("response=2.3s", HumanActionMetadata(HumanActionKind.CHOICE, 2340.milliseconds).toDisplayString())
    }

    @Test
    fun `each kind of action is a separate decision method`() {
        val decisionMethods = HumanActionKind.entries.map { HumanActionMetadata(it, 1.seconds).toSummable().decisionMethod }

        assertEquals(listOf("人間 発言欲求", "人間 発言", "人間 選択"), decisionMethods)
    }

    @Test
    fun `plus adds response times and counts of the same kind`() {
        val a = HumanSummableMetadata(HumanActionKind.SPEAKING_URGE, ResponseTimeStats(total = 3.seconds, max = 2.seconds), count = 2)
        val b = HumanSummableMetadata(HumanActionKind.SPEAKING_URGE, ResponseTimeStats(5.seconds), count = 1)

        assertEquals(
            HumanSummableMetadata(HumanActionKind.SPEAKING_URGE, ResponseTimeStats(total = 8.seconds, max = 5.seconds), count = 3),
            a + b,
        )
    }

    @Test
    fun `plus rejects different kinds of action`() {
        val urge = HumanActionMetadata(HumanActionKind.SPEAKING_URGE, 1.seconds).toSummable()
        val statement = HumanActionMetadata(HumanActionKind.STATEMENT, 1.seconds).toSummable()

        assertNotEquals(urge.decisionMethod, statement.decisionMethod)
        assertFailsWith<IllegalArgumentException> { urge + statement }
    }

    @Test
    fun `plus rejects other kinds of summable`() {
        val urge = HumanActionMetadata(HumanActionKind.SPEAKING_URGE, 1.seconds).toSummable()

        assertFailsWith<IllegalArgumentException> { urge + UnimplementedSummableMetadata(count = 1) }
    }

    @Test
    fun `summable display shows response times`() {
        val summable = HumanSummableMetadata(HumanActionKind.STATEMENT, ResponseTimeStats(total = 30.seconds, max = 20.seconds), count = 2)

        assertEquals("response total 30.0s / avg 15.0s / max 20.0s", summable.toDisplayString())
    }
}
