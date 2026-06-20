package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.RollerCpuPlayer

class RollerCpuLodge(humanConnection: HumanConnection) : CpuLodge(humanConnection) {
    override fun createCpuPlayer(role: Role, name: String) = RollerCpuPlayer(role, name)
}
