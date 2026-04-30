package org.example

open class NothingPlayer(role: Role, override val name: String) : Player(role) {
    override fun discuss(players: List<Player>): Statement = error("discuss not expected")
    override fun onReceive(event: GameEvent) { error("onReceive not expected") }
    override fun selectTarget(context: SelectionContext): Player = error("selectTarget not expected")
}