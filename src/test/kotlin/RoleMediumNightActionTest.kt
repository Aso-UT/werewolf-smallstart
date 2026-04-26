package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class RoleMediumNightActionTest {

    private class StubPlayer(override val name: String, role: Role) : Player(role) {
        override fun selectTarget(context: SelectionContext) = this
        override fun onReceive(event: GameEvent) = Unit
        override fun discuss(players: List<Player>): Statement = Statement.Plain("")
    }

    @Test
    fun `returns None when no executions in knowledge`() {
        val medium = StubPlayer("Medium", Role.MEDIUM)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.None, action)
    }

    @Test
    fun `returns None on first night because no executions yet`() {
        val medium = StubPlayer("Medium", Role.MEDIUM)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = true)

        assertEquals(NightAction.None, action)
    }

    @Test
    fun `reveals executed player when not yet revealed`() {
        val medium = StubPlayer("Medium", Role.MEDIUM)
        val villager = StubPlayer("Villager", Role.VILLAGER)
        GameEvent.PlayerExecuted.send(villager, AllPlayers(listOf(medium, villager)))

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.MediumReveal(villager), action)
    }

    @Test
    fun `returns None when executed player is already revealed`() {
        val medium = StubPlayer("Medium", Role.MEDIUM)
        val villager = StubPlayer("Villager", Role.VILLAGER)
        GameEvent.PlayerExecuted.send(villager, AllPlayers(listOf(medium, villager)))
        GameEvent.MediumRevealed.send(villager, MediumResult.NOT_WEREWOLF, medium)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.None, action)
    }

    @Test
    fun `reveals players in execution order`() {
        val medium = StubPlayer("Medium", Role.MEDIUM)
        val villager1 = StubPlayer("V1", Role.VILLAGER)
        val villager2 = StubPlayer("V2", Role.VILLAGER)
        val allPlayers = AllPlayers(listOf(medium, villager1, villager2))
        GameEvent.PlayerExecuted.send(villager1, allPlayers)
        GameEvent.PlayerExecuted.send(villager2, allPlayers)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.MediumReveal(villager1), action)
    }

    @Test
    fun `reveals next unrevealed player after first is already revealed`() {
        val medium = StubPlayer("Medium", Role.MEDIUM)
        val villager1 = StubPlayer("V1", Role.VILLAGER)
        val villager2 = StubPlayer("V2", Role.VILLAGER)
        val allPlayers = AllPlayers(listOf(medium, villager1, villager2))
        GameEvent.PlayerExecuted.send(villager1, allPlayers)
        GameEvent.MediumRevealed.send(villager1, MediumResult.NOT_WEREWOLF, medium)
        GameEvent.PlayerExecuted.send(villager2, allPlayers)

        val action = medium.buildNightAction(listOf(medium), isFirstNight = false)

        assertEquals(NightAction.MediumReveal(villager2), action)
    }
}
