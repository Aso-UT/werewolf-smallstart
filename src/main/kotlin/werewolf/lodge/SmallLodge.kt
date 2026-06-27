package werewolf.lodge

import werewolf.game.Role
import werewolf.human.HumanPlayer
import werewolf.human.console.ConsoleHumanIO

class SmallLodge(humanConnection: HumanConnection) : Lodge(humanConnection) {
    override fun assignments() = listOf(
        createPlayer(Role.HUNTER,   "1") to Role.HUNTER,
        HumanPlayer(Role.VILLAGER,  "2", ConsoleHumanIO()) to Role.VILLAGER,
        HumanPlayer(Role.VILLAGER,  "3", ConsoleHumanIO()) to Role.VILLAGER,
        HumanPlayer(Role.WEREWOLF,  "4", ConsoleHumanIO()) to Role.WEREWOLF,
        HumanPlayer(Role.SEER,      "5", ConsoleHumanIO()) to Role.SEER,
        HumanPlayer(Role.MEDIUM,    "6", ConsoleHumanIO()) to Role.MEDIUM,
        HumanPlayer(Role.WEREWOLF,  "7", ConsoleHumanIO()) to Role.WEREWOLF,
    )
}
