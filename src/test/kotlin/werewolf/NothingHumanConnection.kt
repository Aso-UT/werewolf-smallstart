package werewolf

import werewolf.game.Player
import werewolf.game.Role
import werewolf.lodge.HumanConnection

object NothingHumanConnection : HumanConnection {
    override fun createPlayer(role: Role, name: String): Player = error("createPlayer must not be called")
    override fun setup(): Nothing = error("setup must not be called")
    override fun teardown(): Nothing = error("teardown must not be called")
}
