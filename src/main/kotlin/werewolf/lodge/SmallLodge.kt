package werewolf.lodge

import werewolf.game.Role
import werewolf.human.console.ConsolePlayerIO
import werewolf.human.HumanPlayer

object SmallLodge : Lodge() {
    override fun assignments() = listOf(
        HumanPlayer(Role.HUNTER,   "1", ConsolePlayerIO()) to Role.HUNTER,
        HumanPlayer(Role.VILLAGER, "2", ConsolePlayerIO()) to Role.VILLAGER,
        HumanPlayer(Role.VILLAGER, "3", ConsolePlayerIO()) to Role.VILLAGER,
        HumanPlayer(Role.WEREWOLF, "4", ConsolePlayerIO()) to Role.WEREWOLF,
        HumanPlayer(Role.SEER,     "5", ConsolePlayerIO()) to Role.SEER,
        HumanPlayer(Role.MEDIUM,   "6", ConsolePlayerIO()) to Role.MEDIUM,
        HumanPlayer(Role.WEREWOLF, "7", ConsolePlayerIO()) to Role.WEREWOLF,
    )
}
