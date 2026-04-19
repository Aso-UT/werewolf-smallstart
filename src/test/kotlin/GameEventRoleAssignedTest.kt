package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class GameEventRoleAssignedTest {

    private val anyPlayer = object : Player {
        override val name = "test"
        override val role = Role.VILLAGER
        override fun selectTarget(context: SelectionContext) = this
        override fun receive(event: GameEvent) {}
        override fun discuss(players: List<Player>) = ""
    }

    @Test
    fun `title is 役職通知`() {
        assertEquals("役職通知", GameEvent.RoleAssigned(Role.WEREWOLF, anyPlayer).title)
    }

    @Test
    fun `body shows role display name`() {
        assertEquals(
            "あなたの役職は「人狼」です。",
            GameEvent.RoleAssigned(Role.WEREWOLF, anyPlayer).body(anyPlayer)
        )
    }
}
