package org.example

object RollerCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = RollerCpuPlayer(role, name)
}
