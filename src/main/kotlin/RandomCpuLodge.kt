package org.example

object RandomCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = RandomCpuPlayer(role, name)
}
