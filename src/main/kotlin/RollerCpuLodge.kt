package org.example

object RollerCpuLodge : Lodge {
    override fun create(): GameSetup {
        val assignments: List<Pair<Role, (Role) -> Player>> = listOf(
            Role.HUNTER   to { HumanPlayer(it, "1", ConsolePlayerIO()) },
            Role.VILLAGER to { RollerCpuPlayer(it, "2") },
            Role.VILLAGER to { RollerCpuPlayer(it, "3") },
            Role.WEREWOLF to { RollerCpuPlayer(it, "4") },
            Role.SEER     to { RollerCpuPlayer(it, "5") },
            Role.MEDIUM   to { RollerCpuPlayer(it, "6") },
            Role.WEREWOLF to { RollerCpuPlayer(it, "7") },
        )
        val playerRoles = assignments.map { (role, factory) -> factory(role) to role }
        return GameSetup(
            players = playerRoles.map { it.first },
            oracle = Oracle(playerRoles.toMap()),
        )
    }
}
