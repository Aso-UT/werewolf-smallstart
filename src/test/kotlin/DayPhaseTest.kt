package org.example

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class DayPhaseTest {

    private class DayPlayer(
        role: Role, name: String, private val voteTarget: Player? = null
    ) : ReceivingPlayer(role, name) {
        var discussedCount = 0
        override fun discuss(players: List<Player>): Statement {
            discussedCount++
            return Statement.Plain("")
        }
        override fun selectTarget(context: SelectionContext): Player =
            voteTarget?.takeIf { it in context.candidates() } ?: context.candidates().first()
    }

    @Test
    fun `each alive player discusses during daytime`() {
        val victim = DayPlayer(Role.VILLAGER, "Victim")
        val wolf = DayPlayer(Role.WEREWOLF, "Wolf", victim)
        val v1 = DayPlayer(Role.VILLAGER, "V1", victim)
        val v2 = DayPlayer(Role.VILLAGER, "V2", victim)
        val v3 = DayPlayer(Role.VILLAGER, "V3", victim)
        val setup = TestLodge(
            wolf to Role.WEREWOLF,
            victim to Role.VILLAGER, v1 to Role.VILLAGER, v2 to Role.VILLAGER, v3 to Role.VILLAGER,
        ).create()

        DayPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertTrue(wolf.discussedCount > 0)
    }

    @Test
    fun `most voted player is executed`() {
        val victim = DayPlayer(Role.VILLAGER, "Victim")
        val wolf = DayPlayer(Role.WEREWOLF, "Wolf", victim)
        val v1 = DayPlayer(Role.VILLAGER, "V1", victim)
        val v2 = DayPlayer(Role.VILLAGER, "V2", victim)
        val v3 = DayPlayer(Role.VILLAGER, "V3", victim)
        val setup = TestLodge(
            wolf to Role.WEREWOLF,
            victim to Role.VILLAGER, v1 to Role.VILLAGER, v2 to Role.VILLAGER, v3 to Role.VILLAGER,
        ).create()

        DayPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertFalse(setup.playerManager.players.contains(victim))
    }

    @Test
    fun `returns NightPhase`() {
        val victim = DayPlayer(Role.VILLAGER, "Victim")
        val wolf = DayPlayer(Role.WEREWOLF, "Wolf", victim)
        val v1 = DayPlayer(Role.VILLAGER, "V1", victim)
        val v2 = DayPlayer(Role.VILLAGER, "V2", victim)
        val v3 = DayPlayer(Role.VILLAGER, "V3", victim)
        val setup = TestLodge(
            wolf to Role.WEREWOLF,
            victim to Role.VILLAGER, v1 to Role.VILLAGER, v2 to Role.VILLAGER, v3 to Role.VILLAGER,
        ).create()

        val next = DayPhase(setup.playerManager, setup.oracle, 1).proceed()

        assertIs<NightPhase>(next)
    }
}
