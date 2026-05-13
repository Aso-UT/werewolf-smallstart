package werewolf

import werewolf.game.*
import werewolf.phase.*
import werewolf.cpu.*
import werewolf.ai.*
import werewolf.human.*
import werewolf.lodge.*

import kotlin.test.Test
import kotlin.test.assertEquals

class RoleMediumNightActionTest {

    @Test
    fun `returns None when no executions in knowledge`() {
        val medium = NothingPlayer(Role.MEDIUM, "Medium")

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.None, action)
    }

    @Test
    fun `returns None on first night because no executions yet`() {
        val medium = NothingPlayer(Role.MEDIUM, "Medium")

        val action = medium.buildNightAction(listOf(medium), isFirstNight = true)

        assertEquals(NightAction.None, action)
    }

    @Test
    fun `reveals executed player when not yet revealed`() {
        val medium = ReceivingPlayer(Role.MEDIUM, "Medium")
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        val allPlayers = AllPlayers(TestLodge(medium to Role.MEDIUM, villager to Role.VILLAGER).create().playerManager)
        GameEvent.PlayerExecuted.send(villager, allPlayers)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.MediumReveal(villager), action)
    }

    @Test
    fun `returns None when executed player is already revealed`() {
        val medium = ReceivingPlayer(Role.MEDIUM, "Medium")
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        val allPlayers = AllPlayers(TestLodge(medium to Role.MEDIUM, villager to Role.VILLAGER).create().playerManager)
        GameEvent.PlayerExecuted.send(villager, allPlayers)
        GameEvent.MediumRevealed.send(villager, MediumResult.NOT_WEREWOLF, medium)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.None, action)
    }

    @Test
    fun `reveals players in execution order`() {
        val medium = ReceivingPlayer(Role.MEDIUM, "Medium")
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val allPlayers = AllPlayers(TestLodge(medium to Role.MEDIUM, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER).create().playerManager)
        GameEvent.PlayerExecuted.send(villager1, allPlayers)
        GameEvent.PlayerExecuted.send(villager2, allPlayers)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.MediumReveal(villager1), action)
    }

    @Test
    fun `reveals next unrevealed player after first is already revealed`() {
        val medium = ReceivingPlayer(Role.MEDIUM, "Medium")
        val villager1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val villager2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val allPlayers = AllPlayers(TestLodge(medium to Role.MEDIUM, villager1 to Role.VILLAGER, villager2 to Role.VILLAGER).create().playerManager)
        GameEvent.PlayerExecuted.send(villager1, allPlayers)
        GameEvent.MediumRevealed.send(villager1, MediumResult.NOT_WEREWOLF, medium)
        GameEvent.PlayerExecuted.send(villager2, allPlayers)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.MediumReveal(villager2), action)
    }
}
