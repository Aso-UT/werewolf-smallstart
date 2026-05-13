package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.HonestCpuPlayer

object HonestCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = HonestCpuPlayer(role, name)
}
