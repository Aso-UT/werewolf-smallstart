package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.RandomCpuPlayer

object RandomCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = RandomCpuPlayer(role, name)
}
