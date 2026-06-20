package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.RandomCpuPlayer

class RandomCpuLodge(humanConnection: HumanConnection) : CpuLodge(humanConnection) {
    override fun createCpuPlayer(role: Role, name: String) = RandomCpuPlayer(role, name)
}
