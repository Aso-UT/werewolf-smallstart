package org.example

object RandomCpuLodge : Lodge {
    override fun create(): GameSetup {
        val assignments: List<Pair<Role, (Role) -> Player>> = listOf(
            Role.HUNTER   to { HumanPlayer(it, "1", ConsolePlayerIO()) },
            Role.VILLAGER to { RandomCpuPlayer(it, "2") },
            Role.VILLAGER to { RandomCpuPlayer(it, "3") },
            Role.WEREWOLF to { RandomCpuPlayer(it, "4") },
            Role.SEER     to { RandomCpuPlayer(it, "5") },
            Role.MEDIUM   to { RandomCpuPlayer(it, "6") },
            Role.WEREWOLF to { RandomCpuPlayer(it, "7") },
        )
        val playerRoles = assignments.map { (role, factory) -> factory(role) to role }
        return GameSetup(
            players = playerRoles.map { it.first },
            oracle = Oracle(playerRoles.toMap()),
        )
    }
}
