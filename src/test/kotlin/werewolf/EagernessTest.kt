package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import werewolf.game.Eagerness
import werewolf.game.StatementType

class EagernessTest {
    private val announcementTypes = setOf(StatementType.ROLE_CLAIM, StatementType.DIVINATION_REPORT, StatementType.MEDIUM_REPORT)

    @Test
    fun `counter claims and announcements allow only claims and reports`() {
        assertEquals(announcementTypes, Eagerness.COUNTER_CLAIM.statementTypes)
        assertEquals(announcementTypes, Eagerness.ANNOUNCEMENT.statementTypes)
    }

    @Test
    fun `eagerness below announcements allows any statement type`() {
        assertEquals(StatementType.entries.toSet(), Eagerness.URGENT.statementTypes)
        assertEquals(StatementType.entries.toSet(), Eagerness.UNWILLING.statementTypes)
    }
}
