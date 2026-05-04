package org.example

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class LodgeTest {

    @Test
    fun `create returns GameSetup with playerManager and oracle`() {
        val villager = NothingPlayer(Role.VILLAGER, "Villager")
        val wolf = NothingPlayer(Role.WEREWOLF, "Wolf")
        val setup = TestLodge(villager to Role.VILLAGER, wolf to Role.WEREWOLF).create()

        val players = setup.playerManager.allPlayers.toList()
        assertSame(villager, players[0])
        assertSame(wolf, players[1])
        assertEquals(listOf(wolf), setup.oracle.werewolves(players))
    }
}
