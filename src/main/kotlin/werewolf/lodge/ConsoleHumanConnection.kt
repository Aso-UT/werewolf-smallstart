package werewolf.lodge

import werewolf.game.Player
import werewolf.game.Role
import werewolf.human.HumanPlayer
import werewolf.human.console.ConsolePlayerIO

class ConsoleHumanConnection : HumanConnection {
    override fun createPlayer(role: Role, name: String): Player = HumanPlayer(role, name, ConsolePlayerIO())
    override fun setup() {}
    override fun teardown() {}
}
