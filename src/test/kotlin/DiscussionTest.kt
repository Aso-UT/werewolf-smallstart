package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class DiscussionTest {

    private class ScriptedPlayer(
        role: Role,
        name: String,
        private val statements: List<String>
    ) : NothingPlayer(role, name) {
        private val _log = mutableListOf<String>()
        val log: List<String> get() = _log
        private var statementIndex = 0

        override fun discuss(players: List<Player>): Statement {
            val statement = statements[statementIndex++]
            _log.add("$name:said:$statement")
            return Statement.Plain(statement)
        }

        override fun onReceive(event: GameEvent) {
            when (event) {
                is GameEvent.StatementMade -> _log.add("$name:heard:${event.speakerName}:${event.statement.text()}")
                else -> error("unexpected event in discussion test: $event")
            }
        }
    }

    private fun fixedOrderDiscussion(alivePlayers: List<Player>, allPlayers: AllPlayers) =
        object : OpenDiscussion(alivePlayers, allPlayers) {
            override fun speakingOrder(speakers: List<Player>) = speakers
        }

    @Test
    fun `statements are delivered immediately in order`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf(
            "私はBobが怪しいと思う",
            "やはりBobだと思う",
            "Bobに投票します"
        ))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf(
            "私は無実です",
            "Aliceこそ怪しい",
            "Aliceに投票します"
        ))
        val players = listOf(alice, bob)
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        fixedOrderDiscussion(players, AllPlayers(playerManager)).conduct()

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
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("村人として発言します", "続けて発言します", "最終発言です"))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("人狼として発言します", "続けて発言します", "最終発言です"))
        val charlie = ScriptedPlayer(Role.VILLAGER, "Charlie", emptyList())
        val alivePlayers = listOf(alice, bob)
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF, charlie to Role.VILLAGER).create().playerManager

        fixedOrderDiscussion(alivePlayers, AllPlayers(playerManager)).conduct()

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
