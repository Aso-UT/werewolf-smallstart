package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotEquals

class RoleAwareCpuPlayerTest {

    private class StubPlayer(override val name: String, role: Role) : Player(role) {
        override fun selectTarget(context: SelectionContext) = this
        override fun onReceive(event: GameEvent) {}
        override fun discuss(players: List<Player>): Statement = Statement.Plain("")
    }

    @Test
    fun `seer reports divination result as DivinationReport`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val villager = StubPlayer("Villager", Role.VILLAGER)
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
        val wolf = StubPlayer("Wolf", Role.WEREWOLF)
        val villager = StubPlayer("Villager", Role.VILLAGER)
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
        val wolf = StubPlayer("Wolf", Role.WEREWOLF)
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
    fun `villager does not vote for player reported as not werewolf when other candidates exist`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val villager = RoleAwareCpuPlayer(Role.VILLAGER, "Villager")
        val innocent = StubPlayer("Innocent", Role.VILLAGER)
        val wolf = StubPlayer("Wolf", Role.WEREWOLF)
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
