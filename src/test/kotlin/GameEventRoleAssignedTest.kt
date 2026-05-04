package org.example

import kotlin.test.Test
import kotlin.test.assertEquals

class GameEventRoleAssignedTest {

    private class RecordingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        val received = mutableListOf<GameEvent>()
        override fun onReceive(event: GameEvent) { received.add(event) }
    }

    @Test
    fun `InitialPhase notifies each player of their assigned role`() {
        val villager = RecordingPlayer(Role.VILLAGER, "Alice")
        val werewolf = RecordingPlayer(Role.WEREWOLF, "Bob")
        val setup = TestLodge(villager to Role.VILLAGER, werewolf to Role.WEREWOLF).create()
        InitialPhase(setup.playerManager, setup.oracle).proceed()

        val villagerEvent = villager.received.single() as GameEvent.RoleAssigned
        assertEquals(Role.VILLAGER, villagerEvent.role)
        assertEquals("あなたの役職は「村人」です。", villagerEvent.body())

        val werewolfEvent = werewolf.received.single() as GameEvent.RoleAssigned
        assertEquals(Role.WEREWOLF, werewolfEvent.role)
        assertEquals("あなたの役職は「人狼」です。", werewolfEvent.body())
    }
}
