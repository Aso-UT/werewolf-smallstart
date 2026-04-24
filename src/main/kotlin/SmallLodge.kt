package org.example

object SmallLodge : Lodge {
    override fun create(): GameSetup {
        val assignments: List<Pair<Role, (Role) -> Player>> = listOf(
            Role.HUNTER  to { HumanPlayer(it, "1", ConsolePlayerIO()) },
            Role.VILLAGER to { HumanPlayer(it, "2", ConsolePlayerIO()) },
            Role.VILLAGER to { HumanPlayer(it, "3", ConsolePlayerIO()) },
            Role.WEREWOLF to { HumanPlayer(it, "4", ConsolePlayerIO()) },
            Role.SEER    to { HumanPlayer(it, "5", ConsolePlayerIO()) },
            Role.MEDIUM  to { HumanPlayer(it, "6", ConsolePlayerIO()) },
            Role.WEREWOLF to { HumanPlayer(it, "7", ConsolePlayerIO()) },
        )
        val playerRoles = assignments.map { (role, factory) -> factory(role) to role }
        return GameSetup(
            players = playerRoles.map { it.first },
            oracle = Oracle(playerRoles.toMap()),
        )
    }
}