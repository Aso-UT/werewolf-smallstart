package werewolf.lodge

import werewolf.game.Player
import werewolf.game.Role

interface HumanConnection {
    fun createPlayer(role: Role, name: String): Player
    fun setup()
    fun teardown()
}
