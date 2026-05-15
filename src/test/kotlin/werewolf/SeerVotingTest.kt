package werewolf

import werewolf.cpu.RoleAwareCpuPlayer
import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SeerVotingTest {

    @Test
    fun `seer does not vote for white-confirmed player when other candidates exist`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(seer to Role.SEER, wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()
        setup.oracle.divine(seer, villager)

        repeat(100) {
            val voted = seer.selectTarget(SelectionContext.Vote(seer, listOf(seer, wolf, villager)))
            assertNotEquals(villager, voted)
        }
    }

    @Test
    fun `seer votes for personally divined wolf`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(seer to Role.SEER, wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()
        setup.oracle.divine(seer, wolf)

        repeat(100) {
            val voted = seer.selectTarget(SelectionContext.Vote(seer, listOf(wolf, villager)))
            assertEquals(wolf, voted)
        }
    }

    @Test
    fun `seer votes for player reported by others as werewolf`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val madman = ReceivingPlayer(Role.MADMAN, "Madman")
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val setup = TestLodge(seer to Role.SEER, madman to Role.MADMAN, villager to Role.VILLAGER, wolf to Role.WEREWOLF).create()
        val allPlayers = AllPlayers(setup.playerManager)
        GameEvent.StatementMade.send(1, madman.name, Statement.DivinationReport(madman, villager, DivineResult.WEREWOLF), allPlayers)

        repeat(100) {
            val voted = seer.selectTarget(SelectionContext.Vote(seer, listOf(villager, madman)))
            assertEquals(villager, voted)
        }
    }

    @Test
    fun `seer falls back to all candidates when all are confirmed or reported innocent`() {
        val seer = RoleAwareCpuPlayer(Role.SEER, "Seer")
        val reporter = ReceivingPlayer(Role.VILLAGER, "Reporter")
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val setup = TestLodge(seer to Role.SEER, reporter to Role.VILLAGER, v1 to Role.VILLAGER, v2 to Role.VILLAGER, wolf to Role.WEREWOLF).create()
        val allPlayers = AllPlayers(setup.playerManager)
        setup.oracle.divine(seer, v1)
        GameEvent.StatementMade.send(1, reporter.name, Statement.DivinationReport(reporter, v2, DivineResult.NOT_WEREWOLF), allPlayers)

        val votes = (1..100).map { seer.selectTarget(SelectionContext.Vote(seer, listOf(v1, v2))) }.toSet()
        assertEquals(setOf(v1, v2), votes)
    }
}
