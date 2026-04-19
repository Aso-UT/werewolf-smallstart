package org.example

interface Notifiable {
    fun receive(event: GameEvent)
}

class AllPlayers(private val players: List<Player>) : Notifiable {
    override fun receive(event: GameEvent) = players.forEach { it.receive(event) }
}