package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class DiscussionTest {

    private class TestPlayer(
        override val name: String,
        role: Role,
        private val statements: List<String>
    ) : Player(role) {
        private val _log = mutableListOf<String>()
        val log: List<String> get() = _log
        private var statementIndex = 0

        override fun discuss(players: List<Player>): String {
            val statement = statements[statementIndex++]
            _log.add("$name:said:$statement")
            return statement
        }

        override fun onReceive(event: GameEvent) {
            when (event) {
                is GameEvent.StatementMade -> _log.add("$name:heard:${event.speakerName}:${event.statement}")
                else -> error("unexpected event in discussion test: $event")
            }
        }

        override fun selectTarget(context: SelectionContext): Player = error("not expected in discussion test")
    }

    private fun fixedOrderDiscussion(alivePlayers: List<Player>, allPlayers: AllPlayers) =
        object : OpenDiscussion(alivePlayers, allPlayers) {
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

        fixedOrderDiscussion(players, AllPlayers(players)).conduct()

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

    @Test
    fun `dead players receive all statements without speaking`() {
        val alice = TestPlayer("Alice", Role.VILLAGER, listOf("村人として発言します", "続けて発言します", "最終発言です"))
        val bob = TestPlayer("Bob", Role.WEREWOLF, listOf("人狼として発言します", "続けて発言します", "最終発言です"))
        val charlie = TestPlayer("Charlie", Role.VILLAGER, emptyList())
        val alivePlayers = listOf(alice, bob)
        val allPlayers = listOf(alice, bob, charlie)

        fixedOrderDiscussion(alivePlayers, AllPlayers(allPlayers)).conduct()

        assertEquals(listOf(
            "Charlie:heard:Alice:村人として発言します",
            "Charlie:heard:Bob:人狼として発言します",
            "Charlie:heard:Alice:続けて発言します",
            "Charlie:heard:Bob:続けて発言します",
            "Charlie:heard:Alice:最終発言です",
            "Charlie:heard:Bob:最終発言です",
        ), charlie.log)
    }
}
