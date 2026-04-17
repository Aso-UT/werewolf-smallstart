package org.example

class Parlor(private val players: List<Player>) {
    companion object {
        private const val ROUNDS = 3
    }

    fun conduct() {
        val speakingOrder = players.shuffled()
        repeat(ROUNDS) { index ->
            speakingOrder.forEach { speaker ->
                val statement = speaker.discuss(players)
                players.forEach { it.receive(GameEvent.StatementMade(index + 1, speaker.name, statement)) }
            }
        }
    }
}
