package werewolf

import werewolf.game.*
import werewolf.phase.*

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InitialPhaseTest {

    private class RecordingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        val received = mutableListOf<GameEvent>()
        override fun onReceive(event: GameEvent) { received.add(event) }
    }

    @Test
    fun `each player is notified of their assigned role`() {
        val villager = RecordingPlayer(Role.VILLAGER, "Villager")
        val wolf = RecordingPlayer(Role.WEREWOLF, "Wolf")
        val setup = TestLodge(villager to Role.VILLAGER, wolf to Role.WEREWOLF).create()

        InitialPhase(setup.playerManager, setup.oracle).proceed()

        assertEquals(Role.VILLAGER, villager.received.filterIsInstance<GameEvent.RoleAssigned>().single().role)
        assertEquals(Role.WEREWOLF, wolf.received.filterIsInstance<GameEvent.RoleAssigned>().single().role)
    }

    @Test
    fun `each wolf is notified of their allies`() {
        val wolf1 = RecordingPlayer(Role.WEREWOLF, "Wolf1")
        val wolf2 = RecordingPlayer(Role.WEREWOLF, "Wolf2")
        val villager = RecordingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf1 to Role.WEREWOLF, wolf2 to Role.WEREWOLF, villager to Role.VILLAGER).create()

        InitialPhase(setup.playerManager, setup.oracle).proceed()

        assertEquals(wolf2, wolf1.received.filterIsInstance<GameEvent.WerewolfAllyRevealed>().single().ally)
        assertEquals(wolf1, wolf2.received.filterIsInstance<GameEvent.WerewolfAllyRevealed>().single().ally)
    }

    @Test
    fun `villager does not receive ally notification`() {
        val wolf = RecordingPlayer(Role.WEREWOLF, "Wolf")
        val villager = RecordingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()

        InitialPhase(setup.playerManager, setup.oracle).proceed()

        assertTrue(villager.received.none { it is GameEvent.WerewolfAllyRevealed })
    }

    @Test
    fun `sole wolf does not receive ally notification`() {
        val wolf = RecordingPlayer(Role.WEREWOLF, "Wolf")
        val villager = RecordingPlayer(Role.VILLAGER, "Villager")
        val setup = TestLodge(wolf to Role.WEREWOLF, villager to Role.VILLAGER).create()

        InitialPhase(setup.playerManager, setup.oracle).proceed()

        assertTrue(wolf.received.none { it is GameEvent.WerewolfAllyRevealed })
    }

    @Test
    fun `all players receive PlayersAnnounced with full player list`() {
        val villager = RecordingPlayer(Role.VILLAGER, "Villager")
        val wolf = RecordingPlayer(Role.WEREWOLF, "Wolf")
        val setup = TestLodge(villager to Role.VILLAGER, wolf to Role.WEREWOLF).create()

        InitialPhase(setup.playerManager, setup.oracle).proceed()

        val announcedNames = villager.received
            .filterIsInstance<GameEvent.PlayersAnnounced>()
            .single()
            .players
            .map { it.name }
        assertTrue(announcedNames.containsAll(listOf("Villager", "Wolf")))
        assertEquals(announcedNames, wolf.received.filterIsInstance<GameEvent.PlayersAnnounced>().single().players.map { it.name })
    }

    @Test
    fun `returns NightPhase`() {
        val setup = TestLodge(
            RecordingPlayer(Role.WEREWOLF, "Wolf") to Role.WEREWOLF,
            RecordingPlayer(Role.VILLAGER, "Villager") to Role.VILLAGER,
        ).create()

        val next = InitialPhase(setup.playerManager, setup.oracle).proceed()

        assertIs<NightPhase>(next)
    }
}
