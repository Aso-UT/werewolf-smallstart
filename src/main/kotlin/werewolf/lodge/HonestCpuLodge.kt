package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.HonestCpuPlayer

class HonestCpuLodge(humanConnection: HumanConnection) : CpuLodge(humanConnection) {
    override fun createCpuPlayer(role: Role, name: String) = HonestCpuPlayer(role, name)
}
