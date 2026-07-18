package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ClaimedRoleTest {

    private fun eventsFor(player: Player): List<Recallable> = player.reveal(fakeCitizenWinSignal())

    @Test
    fun `returns null when player has not claimed anything`() {
        val player = ReceivingPlayer(Role.SEER, "Player")
        val allPlayers = AllPlayers(TestLodge(player to Role.SEER).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.Plain(player, "hello"), allPlayers)

        assertNull(ClaimedRole(eventsFor(player)).of(player))
    }

    @Test
    fun `returns role from RoleClaim statement`() {
        val player = ReceivingPlayer(Role.MADMAN, "Player")
        val allPlayers = AllPlayers(TestLodge(player to Role.MADMAN).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.RoleClaim(player, Role.SEER), allPlayers)

        assertEquals(Role.SEER, ClaimedRole(eventsFor(player)).of(player))
    }

    @Test
    fun `returns SEER from DivinationReport statement`() {
        val player = ReceivingPlayer(Role.SEER, "Player")
        val other = ReceivingPlayer(Role.VILLAGER, "Other")
        val allPlayers = AllPlayers(TestLodge(player to Role.SEER, other to Role.VILLAGER).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.DivinationReport(player, other, DivineResult.NOT_WEREWOLF), allPlayers)

        assertEquals(Role.SEER, ClaimedRole(eventsFor(player)).of(player))
    }

    @Test
    fun `returns MEDIUM from MediumReport statement`() {
        val player = ReceivingPlayer(Role.MEDIUM, "Player")
        val other = ReceivingPlayer(Role.VILLAGER, "Other")
        val allPlayers = AllPlayers(TestLodge(player to Role.MEDIUM, other to Role.VILLAGER).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.MediumReport(player, other, MediumResult.NOT_WEREWOLF), allPlayers)

        assertEquals(Role.MEDIUM, ClaimedRole(eventsFor(player)).of(player))
    }

    @Test
    fun `last claim wins when claimed multiple times`() {
        val player = ReceivingPlayer(Role.MADMAN, "Player")
        val allPlayers = AllPlayers(TestLodge(player to Role.MADMAN).create().playerManager)
        GameEvent.StatementMade.send(1, player.name, Statement.RoleClaim(player, Role.SEER), allPlayers)
        GameEvent.StatementMade.send(2, player.name, Statement.RoleClaim(player, Role.MEDIUM), allPlayers)

        assertEquals(Role.MEDIUM, ClaimedRole(eventsFor(player)).of(player))
    }

    @Test
    fun `ignores claims made by other players`() {
        val player = ReceivingPlayer(Role.SEER, "Player")
        val other = ReceivingPlayer(Role.MADMAN, "Other")
        val allPlayers = AllPlayers(TestLodge(player to Role.SEER, other to Role.MADMAN).create().playerManager)
        GameEvent.StatementMade.send(1, other.name, Statement.RoleClaim(other, Role.SEER), allPlayers)

        assertNull(ClaimedRole(eventsFor(player)).of(player))
    }
}
