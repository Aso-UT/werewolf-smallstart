package org.example

interface Notifiable {
    val recipientName: String
    fun receive(event: GameEvent)
}

class AllPlayers(private val players: List<Player>) : Notifiable, Iterable<Player> {
    override val recipientName = "全プレイヤー"
    override fun receive(event: GameEvent) = players.forEach { it.receive(event) }
    override fun iterator() = players.iterator()
}