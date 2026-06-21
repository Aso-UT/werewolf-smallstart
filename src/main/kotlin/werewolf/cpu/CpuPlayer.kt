package werewolf.cpu

import werewolf.game.ChronicleView
import werewolf.game.Player
import werewolf.game.Role

abstract class CpuPlayer(role: Role) : Player(role) {
    override fun watchEpilogue(chronicles: List<ChronicleView>) = Unit
}
