package org.example

abstract class CpuPlayer(role: Role) : Player(role) {
    override fun watchEpilogue(events: List<GameEvent>) = Unit
}
