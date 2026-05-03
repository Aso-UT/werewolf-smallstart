package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class RoleAwareCpuPlayerTest {

    @Test
    fun `seer reports divination result as DivinationReport`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val oracle = Oracle(mapOf(seer to Role.SEER, villager to Role.VILLAGER))
        oracle.divine(seer, villager)

        val statement = seer.discuss(emptyList())

        val report = assertIs<Statement.DivinationReport>(statement)
        assertEquals(seer, report.claimant)
        assertEquals(villager, report.target)
        assertEquals(DivineResult.NOT_WEREWOLF, report.result)
    }

    @Test
    fun `seer does not vote for white-confirmed player when other candidates exist`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val oracle = Oracle(mapOf(seer to Role.SEER, wolf to Role.WEREWOLF, villager to Role.VILLAGER))
        oracle.divine(seer, villager)

        repeat(100) {
            val voted = seer.selectTarget(SelectionContext.Vote(seer, listOf(seer, wolf, villager)))
            assertNotEquals(villager, voted)
        }
    }

    @Test
    fun `villager votes for player reported as werewolf`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val villager = RoleAwareCpuPlayer(Role.VILLAGER, "Villager")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val oracle = Oracle(mapOf(seer to Role.SEER, villager to Role.VILLAGER, wolf to Role.WEREWOLF))
        val allPlayers = AllPlayers(listOf(seer, villager, wolf))
        oracle.divine(seer, wolf)
        GameEvent.StatementMade.send(1, seer.name, seer.discuss(emptyList()), allPlayers)

        repeat(100) {
            val voted = villager.selectTarget(SelectionContext.Vote(villager, listOf(seer, villager, wolf)))
            assertEquals(wolf, voted)
        }
    }

    @Test
    fun `medium reports medium result as MediumReport`() {
        val medium = RoleAwareCpuPlayer(Role.MEDIUM, "Medium")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val oracle = Oracle(mapOf(medium to Role.MEDIUM, villager to Role.VILLAGER))
        oracle.mediumReveal(medium, villager)

        val statement = medium.discuss(emptyList())

        val report = assertIs<Statement.MediumReport>(statement)
        assertEquals(medium, report.claimant)
        assertEquals(villager, report.target)
        assertEquals(MediumResult.NOT_WEREWOLF, report.result)
    }

    @Test
    fun `werewolf attacks claimed seer`() {
        val wolf = RoleAwareCpuPlayer(Role.WEREWOLF, "Wolf")
        val seer = ReceivingPlayer(Role.SEER, "Seer")
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        val allPlayers = AllPlayers(listOf(wolf, seer, villager))
        GameEvent.StatementMade.send(1, seer.name, Statement.DivinationReport(seer, villager, DivineResult.NOT_WEREWOLF), allPlayers)

        repeat(100) {
            val target = wolf.selectTarget(SelectionContext.Attack(wolf, listOf(seer, villager), emptyList()))
            assertEquals(seer, target)
        }
    }

    @Test
    fun `hunter guards claimed seer`() {
        val hunter = RoleAwareCpuPlayer(Role.HUNTER, "Hunter")
        val seer = ReceivingPlayer(Role.SEER, "Seer")
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        val allPlayers = AllPlayers(listOf(hunter, seer, villager))
        GameEvent.StatementMade.send(1, seer.name, Statement.DivinationReport(seer, villager, DivineResult.NOT_WEREWOLF), allPlayers)

        repeat(100) {
            val target = hunter.selectTarget(SelectionContext.Guard(hunter, listOf(seer, villager)))
            assertEquals(seer, target)
        }
    }

    @Test
    fun `werewolf votes for claimed seer`() {
        val wolf = RoleAwareCpuPlayer(Role.WEREWOLF, "Wolf")
        val seer = ReceivingPlayer(Role.SEER, "Seer")
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        val allPlayers = AllPlayers(listOf(wolf, seer, villager))
        GameEvent.StatementMade.send(1, seer.name, Statement.DivinationReport(seer, villager, DivineResult.NOT_WEREWOLF), allPlayers)

        repeat(100) {
            val voted = wolf.selectTarget(SelectionContext.Vote(wolf, listOf(seer, villager)))
            assertEquals(seer, voted)
        }
    }

    @Test
    fun `all roles have a strategy in RoleAwareCpuPlayer`() {
        Role.entries.forEach { role ->
            RoleAwareCpuPlayer(role, "test")
        }
    }

    @Test
    fun `villager does not vote for player reported as not werewolf when other candidates exist`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val villager = RoleAwareCpuPlayer(Role.VILLAGER, "Villager")
        val innocent = ReceivingPlayer(Role.VILLAGER, "Innocent")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val oracle = Oracle(mapOf(seer to Role.SEER, villager to Role.VILLAGER, innocent to Role.VILLAGER, wolf to Role.WEREWOLF))
        val allPlayers = AllPlayers(listOf(seer, villager, innocent, wolf))
        oracle.divine(seer, innocent)
        GameEvent.StatementMade.send(1, seer.name, seer.discuss(emptyList()), allPlayers)

        repeat(100) {
            val voted = villager.selectTarget(SelectionContext.Vote(villager, listOf(seer, villager, innocent, wolf)))
            assertNotEquals(innocent, voted)
        }
    }
}
