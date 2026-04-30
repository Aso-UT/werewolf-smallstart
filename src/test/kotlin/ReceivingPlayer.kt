package org.example

open class ReceivingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
    override fun onReceive(event: GameEvent) {}
}