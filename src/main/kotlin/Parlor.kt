package org.example

abstract class Parlor(private val players: List<Player>) {
    companion object {
        private const val ROUNDS = 3
    }

    protected abstract fun speakingOrder(players: List<Player>): List<Player>

    fun conduct() {
        val order = speakingOrder(players)
        repeat(ROUNDS) { index ->
            order.forEach { speaker ->
                val statement = speaker.discuss(players)
                players.forEach { it.receive(GameEvent.StatementMade(index + 1, speaker.name, statement)) }
            }
        }
    }
}

class RandomParlor(players: List<Player>) : Parlor(players) {
    override fun speakingOrder(players: List<Player>) = players.shuffled()
}
