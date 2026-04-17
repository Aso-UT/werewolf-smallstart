package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParlorTest {

    private class TestPlayer(
        override val name: String,
        override val role: Role,
        private val fixedStatement: String
    ) : Player {
        private val _log = mutableListOf<String>()
        val log: List<String> get() = _log

        override fun discuss(players: List<Player>): String {
            _log.add("$name:said:$fixedStatement")
            return fixedStatement
        }

        override fun receive(event: GameEvent) {
            when (event) {
                is GameEvent.StatementMade -> _log.add("$name:heard:${event.speakerName}:${event.statement}")
                else -> error("unexpected event in discussion test: $event")
            }
        }

        override fun selectTarget(context: SelectionContext): Player = error("not expected in discussion test")
    }

    @Test
    fun `each player immediately receives their own statement after speaking`() {
        val players = listOf(
            TestPlayer("A", Role.VILLAGER, "msg-A"),
            TestPlayer("B", Role.WEREWOLF, "msg-B")
        )

        Parlor(players).conduct()

        players.forEach { player ->
            val log = player.log
            log.indices
                .filter { log[it].startsWith("${player.name}:said:") }
                .forEach { i ->
                    assertTrue(log[i + 1].startsWith("${player.name}:heard:${player.name}:"))
                }
        }
    }

    @Test
    fun `non-first speakers hear all preceding statements before speaking`() {
        val players = listOf(
            TestPlayer("A", Role.VILLAGER, "msg-A"),
            TestPlayer("B", Role.WEREWOLF, "msg-B")
        )

        Parlor(players).conduct()

        players.forEach { player ->
            val log = player.log
            val firstSaidIndex = log.indexOfFirst { it.startsWith("${player.name}:said:") }
            if (firstSaidIndex > 0) {
                assertTrue(log.subList(0, firstSaidIndex).all { it.startsWith("${player.name}:heard:") })
            }
        }
    }
}
