package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class ConclaveTest {

    private class TestPlayer(
        override val name: String,
        role: Role,
        private val statements: List<String>
    ) : Player(role) {
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
                is GameEvent.WerewolfStatementMade -> _log.add("$name:heard:${event.speakerName}:${event.statement}")
                else -> error("unexpected event in conclave test: $event")
            }
        }

        override fun selectTarget(context: SelectionContext): Player = error("not expected in conclave test")
    }

    private fun fixedOrderConclave(oracle: Oracle, playerManager: PlayerManager) =
        object : Conclave(oracle, playerManager) {
            override fun speakingOrder(speakers: List<Player>) = speakers
        }

    @Test
    fun `single werewolf does not speak`() {
        val alpha = TestPlayer("Alpha", Role.WEREWOLF, emptyList())
        val wolves = listOf(alpha)
        val oracle = Oracle(wolves.associateWith { Role.WEREWOLF })
        val playerManager = PlayerManager(GameSetup(wolves, oracle))

        fixedOrderConclave(oracle, playerManager).conduct()

        assertEquals(emptyList(), alpha.log)
    }

    @Test
    fun `statements are delivered to all wolves in round order`() {
        val alpha = TestPlayer("Alpha", Role.WEREWOLF, listOf("r1a", "r2a", "r3a"))
        val beta = TestPlayer("Beta", Role.WEREWOLF, listOf("r1b", "r2b", "r3b"))
        val wolves = listOf(alpha, beta)
        val oracle = Oracle(wolves.associateWith { Role.WEREWOLF })
        val playerManager = PlayerManager(GameSetup(wolves, oracle))

        fixedOrderConclave(oracle, playerManager).conduct()

        assertEquals(listOf(
            "Alpha:said:r1a",
            "Alpha:heard:Alpha:r1a",
            "Alpha:heard:Beta:r1b",
            "Alpha:said:r2a",
            "Alpha:heard:Alpha:r2a",
            "Alpha:heard:Beta:r2b",
            "Alpha:said:r3a",
            "Alpha:heard:Alpha:r3a",
            "Alpha:heard:Beta:r3b",
        ), alpha.log)

        assertEquals(listOf(
            "Beta:heard:Alpha:r1a",
            "Beta:said:r1b",
            "Beta:heard:Beta:r1b",
            "Beta:heard:Alpha:r2a",
            "Beta:said:r2b",
            "Beta:heard:Beta:r2b",
            "Beta:heard:Alpha:r3a",
            "Beta:said:r3b",
            "Beta:heard:Beta:r3b",
        ), beta.log)
    }
}