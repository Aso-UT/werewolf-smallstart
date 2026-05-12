package org.example

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlayerTest {

    private class ArbitraryChooserSetter(
        role: Role,
        name: String,
        private val chooserToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(chooserToSet, context)
    }

    private class ChoosingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(this, context)
    }

    @Test
    fun `selectTarget throws when choose returns a choice with wrong chooser`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitraryChooserSetter(Role.VILLAGER, "Player", chooserToSet = otherPlayer)
        val context = SelectionContext.Vote(player, listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.selectTarget(context) }
    }

    @Test
    fun `selectTarget records choice in player memories`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ChoosingPlayer(Role.VILLAGER, "Player")
        val context = SelectionContext.Vote(player, listOf(player, otherPlayer))
        player.selectTarget(context)
        assertTrue(player.reveal(fakeCitizenWinSignal()).any { it is Choice })
    }
}
