package werewolf.cpu

import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.Role

abstract class CpuPlayer(role: Role) : Player(role) {
    override fun watchEpilogue(chronicles: List<Recallable>) = Unit
}
