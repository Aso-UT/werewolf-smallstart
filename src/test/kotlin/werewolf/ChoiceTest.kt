package werewolf

import werewolf.game.*
import werewolf.phase.*
import werewolf.cpu.*
import werewolf.ai.*
import werewolf.human.*
import werewolf.lodge.*

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ChoiceTest {

    @Test
    fun `Choice throws when selected is not in candidates`() {
        val chooser = NothingPlayer(Role.VILLAGER, "Chooser")
        val selectable = NothingPlayer(Role.VILLAGER, "Selectable")
        val outsider = NothingPlayer(Role.VILLAGER, "Outsider")
        val context = SelectionContext.Vote(chooser, listOf(chooser, selectable))
        assertFailsWith<IllegalArgumentException> {
            Choice(chooser, context, outsider, "理由")
        }
    }

    @Test
    fun `Choice does not throw when selected is in candidates`() {
        val chooser = NothingPlayer(Role.VILLAGER, "Chooser")
        val selectable = NothingPlayer(Role.VILLAGER, "Selectable")
        val context = SelectionContext.Vote(chooser, listOf(chooser, selectable))
        assertNotNull(Choice(chooser, context, selectable, "理由"))
    }
}
