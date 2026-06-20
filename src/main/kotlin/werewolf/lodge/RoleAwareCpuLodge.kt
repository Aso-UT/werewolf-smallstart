package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.RoleAwareCpuPlayer

class RoleAwareCpuLodge(humanConnection: HumanConnection) : CpuLodge(humanConnection) {
    override fun createCpuPlayer(role: Role, name: String) = RoleAwareCpuPlayer(role, name)
}
