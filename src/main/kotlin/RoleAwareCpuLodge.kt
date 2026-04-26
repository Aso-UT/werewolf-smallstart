package org.example

object RoleAwareCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = RoleAwareCpuPlayer(role, name)
}
