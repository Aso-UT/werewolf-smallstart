package org.example

abstract class Discussion(
    private val speakers: List<Player>,
    private val recipients: AllPlayers
) {
    companion object {
        private const val ROUNDS = 3
    }

    protected abstract fun speakingOrder(players: List<Player>): List<Player>
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: String, recipients: AllPlayers)

    fun conduct() {
        val order = speakingOrder(speakers)
        repeat(ROUNDS) { index ->
            order.forEach { speaker ->
                val statement = speaker.discuss(speakers)
                sendStatement(index + 1, speaker.name, statement, recipients)
            }
        }
    }
}

open class OpenDiscussion(speakers: List<Player>, recipients: AllPlayers) : Discussion(speakers, recipients) {
    override fun speakingOrder(players: List<Player>) = players.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: String, recipients: AllPlayers) =
        GameEvent.StatementMade.send(round, speakerName, statement, recipients)
}
