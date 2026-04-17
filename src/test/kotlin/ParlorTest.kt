package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class ParlorTest {

    private class TestPlayer(
        override val name: String,
        override val role: Role,
        private val statements: List<String>
    ) : Player {
        private val _log = mutableListOf<String>()
        val log: List<String> get() = _log
        private var statementIndex = 0

        override fun discuss(players: List<Player>): String {
            val statement = statements[statementIndex++]
            _log.add("$name:said:$statement")
            return statement
        }

        override fun receive(event: GameEvent) {
            when (event) {
                is GameEvent.StatementMade -> _log.add("$name:heard:${event.speakerName}:${event.statement}")
                else -> error("unexpected event in discussion test: $event")
            }
        }

        override fun selectTarget(context: SelectionContext): Player = error("not expected in discussion test")
    }

    private fun fixedOrderParlor(players: List<Player>) = object : Parlor(players) {
        override fun speakingOrder(players: List<Player>) = players
    }

    @Test
    fun `statements are delivered immediately in order`() {
        val alice = TestPlayer("Alice", Role.VILLAGER, listOf(
            "私はBobが怪しいと思う",
            "やはりBobだと思う",
            "Bobに投票します"
        ))
        val bob = TestPlayer("Bob", Role.WEREWOLF, listOf(
            "私は無実です",
            "Aliceこそ怪しい",
            "Aliceに投票します"
        ))
        val players = listOf(alice, bob)

        fixedOrderParlor(players).conduct()

        assertEquals(listOf(
            "Alice:said:私はBobが怪しいと思う",
            "Alice:heard:Alice:私はBobが怪しいと思う",
            "Alice:heard:Bob:私は無実です",
            "Alice:said:やはりBobだと思う",
            "Alice:heard:Alice:やはりBobだと思う",
            "Alice:heard:Bob:Aliceこそ怪しい",
            "Alice:said:Bobに投票します",
            "Alice:heard:Alice:Bobに投票します",
            "Alice:heard:Bob:Aliceに投票します",
        ), alice.log)

        assertEquals(listOf(
            "Bob:heard:Alice:私はBobが怪しいと思う",
            "Bob:said:私は無実です",
            "Bob:heard:Bob:私は無実です",
            "Bob:heard:Alice:やはりBobだと思う",
            "Bob:said:Aliceこそ怪しい",
            "Bob:heard:Bob:Aliceこそ怪しい",
            "Bob:heard:Alice:Bobに投票します",
            "Bob:said:Aliceに投票します",
            "Bob:heard:Bob:Aliceに投票します",
        ), bob.log)
    }
}
