package werewolf.lodge

import werewolf.game.Player
import werewolf.game.Role
import werewolf.human.HumanPlayer
import werewolf.human.console.ConsoleHumanIO

class ConsoleHumanConnection : HumanConnection {
    override fun createPlayer(role: Role, name: String): Player = HumanPlayer(role, name, ConsoleHumanIO())
    override fun setup() { /* Console I/O is managed by the JVM and requires no setup */ }
    override fun teardown() { /* Console I/O is managed by the JVM and requires no teardown */ }
}
