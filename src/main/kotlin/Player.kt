package org.example

interface Player {
    val name: String
    val role: Role
    fun selectTarget(context: SelectionContext): Player
    fun receive(event: GameEvent)
    fun discuss(players: List<Player>): String
}