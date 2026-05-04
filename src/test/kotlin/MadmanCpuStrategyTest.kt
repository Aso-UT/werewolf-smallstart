package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MadmanCpuStrategyTest {

    private val madman = RoleAwareCpuPlayer(Role.MADMAN, "狂人")
    private val player1 = ReceivingPlayer(Role.VILLAGER, "村人1")
    private val player2 = ReceivingPlayer(Role.VILLAGER, "村人2")
    private val allPlayers = AllPlayers(
        TestLodge(madman to Role.MADMAN, player1 to Role.VILLAGER, player2 to Role.VILLAGER).create().playerManager
    )

    @Test
    fun `fake seer reports an alive player as werewolf`() {
        val strategy = MadmanCpuStrategy(madman, Role.SEER)
        val result = strategy.discuss(listOf(madman, player1, player2))
        assertTrue(result is Statement.DivinationReport)
        assertEquals(DivineResult.WEREWOLF, result.result)
        assertNotSame(madman, result.target)
    }

    @Test
    fun `fake seer stays silent when all players already accused`() {
        val strategy = MadmanCpuStrategy(madman, Role.SEER)
        GameEvent.StatementMade.send(1, madman.name, Statement.DivinationReport(madman, player1, DivineResult.WEREWOLF), allPlayers)
        GameEvent.StatementMade.send(1, madman.name, Statement.DivinationReport(madman, player2, DivineResult.WEREWOLF), allPlayers)

        val result = strategy.discuss(listOf(madman, player1, player2))
        assertTrue(result is Statement.Plain)
    }

    @Test
    fun `fake medium reports executed player as not werewolf`() {
        val strategy = MadmanCpuStrategy(madman, Role.MEDIUM)
        GameEvent.PlayerExecuted.send(player1, allPlayers)

        val result = strategy.discuss(listOf(madman, player2))
        assertTrue(result is Statement.MediumReport)
        assertEquals(MediumResult.NOT_WEREWOLF, result.result)
        assertSame(player1, result.target)
    }

    @Test
    fun `fake medium stays silent when no executions yet`() {
        val strategy = MadmanCpuStrategy(madman, Role.MEDIUM)
        val result = strategy.discuss(listOf(madman, player1, player2))
        assertTrue(result is Statement.Plain)
    }

    @Test
    fun `fake medium stays silent after all executed players already reported`() {
        val strategy = MadmanCpuStrategy(madman, Role.MEDIUM)
        GameEvent.PlayerExecuted.send(player1, allPlayers)
        GameEvent.StatementMade.send(1, madman.name, Statement.MediumReport(madman, player1, MediumResult.NOT_WEREWOLF), allPlayers)

        val result = strategy.discuss(listOf(madman, player2))
        assertTrue(result is Statement.Plain)
    }
}
