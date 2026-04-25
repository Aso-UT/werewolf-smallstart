package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class GameEventRoleAssignedTest {

    private class RecordingPlayer(role: Role, override val name: String) : Player(role) {
        val received = mutableListOf<GameEvent>()
        override fun selectTarget(context: SelectionContext) = this
        override fun onReceive(event: GameEvent) { received.add(event) }
        override fun discuss(players: List<Player>) = ""
    }

    @Test
    fun `startGame notifies each player of their assigned role`() {
        val villager = RecordingPlayer(Role.VILLAGER, "Alice")
        val werewolf = RecordingPlayer(Role.WEREWOLF, "Bob")
        val setup = GameSetup(
            players = listOf(villager, werewolf),
            oracle = Oracle(mapOf(villager to Role.VILLAGER, werewolf to Role.WEREWOLF)),
        )
        PlayerManager(setup).startGame()

        val villagerEvent = villager.received.single() as GameEvent.RoleAssigned
        assertEquals(Role.VILLAGER, villagerEvent.role)
        assertEquals("あなたの役職は「村人」です。", villagerEvent.body(villager))

        val werewolfEvent = werewolf.received.single() as GameEvent.RoleAssigned
        assertEquals(Role.WEREWOLF, werewolfEvent.role)
        assertEquals("あなたの役職は「人狼」です。", werewolfEvent.body(werewolf))
    }
}
