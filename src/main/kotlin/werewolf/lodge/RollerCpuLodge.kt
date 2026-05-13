package werewolf.lodge

import werewolf.game.Role
import werewolf.cpu.RollerCpuPlayer

object RollerCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = RollerCpuPlayer(role, name)
}
