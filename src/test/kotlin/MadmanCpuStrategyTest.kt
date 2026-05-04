package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MadmanCpuStrategyTest {

    private val madman = RoleAwareCpuPlayer(Role.MADMAN, "Madman")
    private val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
    private val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
    private val allPlayers = AllPlayers(
        TestLodge(madman to Role.MADMAN, v1 to Role.VILLAGER, v2 to Role.VILLAGER).create().playerManager
    )

    @Test
    fun `fake seer reports an alive player as werewolf`() {
        val strategy = MadmanCpuStrategy(madman, Role.SEER)
        val result = strategy.discuss(listOf(madman, v1, v2))
        assertTrue(result is Statement.DivinationReport)
        assertEquals(DivineResult.WEREWOLF, result.result)
        assertNotSame(madman, result.target)
    }

    @Test
    fun `fake seer stays silent when all players already accused`() {
        val strategy = MadmanCpuStrategy(madman, Role.SEER)
        GameEvent.StatementMade.send(1, madman.name, Statement.DivinationReport(madman, v1, DivineResult.WEREWOLF), allPlayers)
        GameEvent.StatementMade.send(1, madman.name, Statement.DivinationReport(madman, v2, DivineResult.WEREWOLF), allPlayers)

        val result = strategy.discuss(listOf(madman, v1, v2))
        assertTrue(result is Statement.Plain)
    }

    @Test
    fun `fake medium reports executed player as not werewolf`() {
        val strategy = MadmanCpuStrategy(madman, Role.MEDIUM)
        GameEvent.PlayerExecuted.send(v1, allPlayers)

        val result = strategy.discuss(listOf(madman, v2))
        assertTrue(result is Statement.MediumReport)
        assertEquals(MediumResult.NOT_WEREWOLF, result.result)
        assertSame(v1, result.target)
    }

    @Test
    fun `fake medium stays silent when no executions yet`() {
        val strategy = MadmanCpuStrategy(madman, Role.MEDIUM)
        val result = strategy.discuss(listOf(madman, v1, v2))
        assertTrue(result is Statement.Plain)
    }

    @Test
    fun `fake medium stays silent after all executed players already reported`() {
        val strategy = MadmanCpuStrategy(madman, Role.MEDIUM)
        GameEvent.PlayerExecuted.send(v1, allPlayers)
        GameEvent.StatementMade.send(1, madman.name, Statement.MediumReport(madman, v1, MediumResult.NOT_WEREWOLF), allPlayers)

        val result = strategy.discuss(listOf(madman, v2))
        assertTrue(result is Statement.Plain)
    }
}
