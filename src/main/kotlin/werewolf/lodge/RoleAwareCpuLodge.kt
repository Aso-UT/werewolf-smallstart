package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.RoleAwareCpuPlayer

object RoleAwareCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = RoleAwareCpuPlayer(role, name)
}
