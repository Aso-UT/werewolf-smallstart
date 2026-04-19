package org.example

interface Player : Notifiable {
    val name: String
    val role: Role
    fun selectTarget(context: SelectionContext): Player
    override fun receive(event: GameEvent)
    fun discuss(players: List<Player>): String
}