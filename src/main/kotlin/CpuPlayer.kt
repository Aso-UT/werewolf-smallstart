package org.example

abstract class CpuPlayer(role: Role) : Player(role) {
    override fun watchEpilogue(chronicles: List<Recallable>) = Unit
}
