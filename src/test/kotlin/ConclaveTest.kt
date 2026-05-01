package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConclaveTest {

    private class ScriptedPlayer(
        role: Role,
        name: String,
        private val statements: List<String>,
        private val receivesExecutionEvent: Boolean = false,
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
            when {
                event is GameEvent.WerewolfStatementMade -> _log.add("$name:heard:${event.speakerName}:${event.statement}")
                event is GameEvent.PlayerExecuted && receivesExecutionEvent -> {}
                else -> error("unexpected event in conclave test: $event")
            }
        }
    }

    private fun fixedOrderConclave(oracle: Oracle, playerManager: PlayerManager) =
        object : Conclave(oracle, playerManager) {
            override fun speakingOrder(speakers: List<Player>) = speakers
        }

    @Test
    fun `non-werewolf neither speaks nor listens`() {
        val alpha = ScriptedPlayer(Role.WEREWOLF, "Alpha", listOf("r1a", "r2a", "r3a"))
        val beta = ScriptedPlayer(Role.WEREWOLF, "Beta", listOf("r1b", "r2b", "r3b"))
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val players = listOf(alpha, beta, villager)
        val oracle = Oracle(mapOf(alpha to Role.WEREWOLF, beta to Role.WEREWOLF, villager to Role.VILLAGER))
        val playerManager = PlayerManager(GameSetup(players, oracle))

        fixedOrderConclave(oracle, playerManager).conduct()

        // villager の discuss・onReceive が呼ばれた場合は NothingPlayer がエラーを投げる
    }

    @Test
    fun `dead werewolf listens but does not speak`() {
        val alpha = ScriptedPlayer(Role.WEREWOLF, "Alpha", listOf("r1a", "r2a", "r3a"), receivesExecutionEvent = true)
        val gamma = ScriptedPlayer(Role.WEREWOLF, "Gamma", listOf("r1g", "r2g", "r3g"), receivesExecutionEvent = true)
        val beta = ScriptedPlayer(Role.WEREWOLF, "Beta", emptyList(), receivesExecutionEvent = true)
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val v3 = ReceivingPlayer(Role.VILLAGER, "V3")
        val players = listOf(alpha, gamma, beta, v1, v2, v3)
        val oracle = Oracle(mapOf(
            alpha to Role.WEREWOLF, gamma to Role.WEREWOLF, beta to Role.WEREWOLF,
            v1 to Role.VILLAGER, v2 to Role.VILLAGER, v3 to Role.VILLAGER,
        ))
        val playerManager = PlayerManager(GameSetup(players, oracle))
        playerManager.execute(beta)

        fixedOrderConclave(oracle, playerManager).conduct()

        assertEquals(listOf(
            "Beta:heard:Alpha:r1a", "Beta:heard:Gamma:r1g",
            "Beta:heard:Alpha:r2a", "Beta:heard:Gamma:r2g",
            "Beta:heard:Alpha:r3a", "Beta:heard:Gamma:r3g",
        ), beta.log)
    }

    @Test
    fun `single werewolf does not speak`() {
        val alpha = ScriptedPlayer(Role.WEREWOLF, "Alpha", emptyList())
        val wolves = listOf(alpha)
        val oracle = Oracle(wolves.associateWith { Role.WEREWOLF })
        val playerManager = PlayerManager(GameSetup(wolves, oracle))

        fixedOrderConclave(oracle, playerManager).conduct()

        assertEquals(emptyList(), alpha.log)
    }

    @Test
    fun `statements are delivered to all wolves in round order`() {
        val alpha = ScriptedPlayer(Role.WEREWOLF, "Alpha", listOf("r1a", "r2a", "r3a"))
        val beta = ScriptedPlayer(Role.WEREWOLF, "Beta", listOf("r1b", "r2b", "r3b"))
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