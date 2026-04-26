package org.example

abstract class CpuLodge : Lodge {
    protected abstract fun createCpuPlayer(role: Role, name: String): Player

    override fun create(): GameSetup {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        val players = listOf(HumanPlayer(roles[0], "1", ConsolePlayerIO())) +
            roles.drop(1).mapIndexed { i, role -> createCpuPlayer(role, "${i + 2}") }
        return GameSetup(
            players = players,
            oracle = Oracle(players.zip(roles).toMap()),
        )
    }
}
