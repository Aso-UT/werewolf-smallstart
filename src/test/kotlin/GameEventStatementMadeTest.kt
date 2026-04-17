package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class GameEventStatementMadeTest {

    private val anyPlayer = object : Player {
        override val name = "test"
        override val role = Role.VILLAGER
        override fun selectTarget(context: SelectionContext) = this
        override fun receive(event: GameEvent) {}
        override fun discuss(players: List<Player>) = ""
    }

    @Test
    fun `title includes round number`() {
        assertEquals("発言（2ラウンド目）", GameEvent.StatementMade(2, "Alice", "私はBobが怪しいと思う。").title)
    }

    @Test
    fun `body shows speaker name and statement`() {
        assertEquals("Alice: 私はBobが怪しいと思う。", GameEvent.StatementMade(1, "Alice", "私はBobが怪しいと思う。").body(anyPlayer))
    }
}
