package org.example

abstract class Discussion(private val alivePlayers: List<Player>, private val allPlayers: List<Player>) {
    companion object {
        private const val ROUNDS = 3
    }

    protected abstract fun speakingOrder(players: List<Player>): List<Player>

    fun conduct() {
        val order = speakingOrder(alivePlayers)
        val recipients = AllPlayers(allPlayers)
        repeat(ROUNDS) { index ->
            order.forEach { speaker ->
                val statement = speaker.discuss(alivePlayers)
                GameEvent.StatementMade(index + 1, speaker.name, statement, recipients).dispatch()
            }
        }
    }
}

class RandomOrderDiscussion(alivePlayers: List<Player>, allPlayers: List<Player>) : Discussion(alivePlayers, allPlayers) {
    override fun speakingOrder(players: List<Player>) = players.shuffled()
}
