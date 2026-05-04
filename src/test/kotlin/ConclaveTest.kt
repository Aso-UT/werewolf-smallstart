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
        val wolf1 = ScriptedPlayer(Role.WEREWOLF, "Wolf1", listOf("r1a", "r2a", "r3a"))
        val wolf2 = ScriptedPlayer(Role.WEREWOLF, "Wolf2", listOf("r1b", "r2b", "r3b"))
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf1 to Role.WEREWOLF, wolf2 to Role.WEREWOLF, villager to Role.VILLAGER).create()

        fixedOrderConclave(setup.oracle, setup.playerManager).conduct()

        // villager の discuss・onReceive が呼ばれた場合は NothingPlayer がエラーを投げる
    }

    @Test
    fun `dead werewolf listens but does not speak`() {
        val wolf1 = ScriptedPlayer(Role.WEREWOLF, "Wolf1", listOf("r1a", "r2a", "r3a"), receivesExecutionEvent = true)
        val wolf2 = ScriptedPlayer(Role.WEREWOLF, "Wolf2", listOf("r1g", "r2g", "r3g"), receivesExecutionEvent = true)
        val wolf3 = ScriptedPlayer(Role.WEREWOLF, "Wolf3", emptyList(), receivesExecutionEvent = true)
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val v3 = ReceivingPlayer(Role.VILLAGER, "V3")
        val setup = TestLodge(
            wolf1 to Role.WEREWOLF, wolf2 to Role.WEREWOLF, wolf3 to Role.WEREWOLF,
            v1 to Role.VILLAGER, v2 to Role.VILLAGER, v3 to Role.VILLAGER,
        ).create()
        setup.playerManager.execute(wolf3)

        fixedOrderConclave(setup.oracle, setup.playerManager).conduct()

        assertEquals(listOf(
            "Wolf3:heard:Wolf1:r1a", "Wolf3:heard:Wolf2:r1g",
            "Wolf3:heard:Wolf1:r2a", "Wolf3:heard:Wolf2:r2g",
            "Wolf3:heard:Wolf1:r3a", "Wolf3:heard:Wolf2:r3g",
        ), wolf3.log)
    }

    @Test
    fun `single werewolf does not speak`() {
        val wolf1 = ScriptedPlayer(Role.WEREWOLF, "Wolf1", emptyList())
        val setup = TestLodge(wolf1 to Role.WEREWOLF).create()

        fixedOrderConclave(setup.oracle, setup.playerManager).conduct()

        assertEquals(emptyList(), wolf1.log)
    }

    @Test
    fun `statements are delivered to all wolves in round order`() {
        val wolf1 = ScriptedPlayer(Role.WEREWOLF, "Wolf1", listOf("r1a", "r2a", "r3a"))
        val wolf2 = ScriptedPlayer(Role.WEREWOLF, "Wolf2", listOf("r1b", "r2b", "r3b"))
        val setup = TestLodge(wolf1 to Role.WEREWOLF, wolf2 to Role.WEREWOLF).create()

        fixedOrderConclave(setup.oracle, setup.playerManager).conduct()

        assertEquals(listOf(
            "Wolf1:said:r1a",
            "Wolf1:heard:Wolf1:r1a",
            "Wolf1:heard:Wolf2:r1b",
            "Wolf1:said:r2a",
            "Wolf1:heard:Wolf1:r2a",
            "Wolf1:heard:Wolf2:r2b",
            "Wolf1:said:r3a",
            "Wolf1:heard:Wolf1:r3a",
            "Wolf1:heard:Wolf2:r3b",
        ), wolf1.log)

        assertEquals(listOf(
            "Wolf2:heard:Wolf1:r1a",
            "Wolf2:said:r1b",
            "Wolf2:heard:Wolf2:r1b",
            "Wolf2:heard:Wolf1:r2a",
            "Wolf2:said:r2b",
            "Wolf2:heard:Wolf2:r2b",
            "Wolf2:heard:Wolf1:r3a",
            "Wolf2:said:r3b",
            "Wolf2:heard:Wolf2:r3b",
        ), wolf2.log)
    }
}
