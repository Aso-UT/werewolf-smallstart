package org.example

object SmallLodge : Lodge {
    override fun create(): List<Player> = listOf(
        HumanPlayer(Role.VILLAGER, "1", ConsolePlayerIO()),
        HumanPlayer(Role.VILLAGER, "2", ConsolePlayerIO()),
        HumanPlayer(Role.VILLAGER, "3", ConsolePlayerIO()),
        HumanPlayer(Role.WEREWOLF, "4", ConsolePlayerIO()),
        HumanPlayer(Role.SEER, "5", ConsolePlayerIO()),
        HumanPlayer(Role.MEDIUM, "6", ConsolePlayerIO()),
    )
}