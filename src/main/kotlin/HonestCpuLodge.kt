package org.example

object HonestCpuLodge : CpuLodge() {
    override fun createCpuPlayer(role: Role, name: String) = HonestCpuPlayer(role, name)
}
