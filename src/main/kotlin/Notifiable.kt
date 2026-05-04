package org.example

interface Notifiable {
    val recipientName: String
    fun receive(event: GameEvent)
}

class AllPlayers(playerManager: PlayerManager) : Notifiable {
    private val players = playerManager.allPlayers
    override val recipientName = "全プレイヤー"
    override fun receive(event: GameEvent) = players.forEach { it.receive(event) }
}

class Wolves(private val oracle: Oracle, private val playerManager: PlayerManager) : Notifiable {
    override val recipientName = "全人狼"
    override fun receive(event: GameEvent) =
        oracle.werewolves(playerManager.allPlayers).forEach { it.receive(event) }
}
