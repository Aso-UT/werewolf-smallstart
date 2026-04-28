package org.example

abstract class Discussion(private val speakers: List<Player>) {
    protected abstract val rounds: Int
    protected abstract fun speakingOrder(speakers: List<Player>): List<Player>
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: Statement)

    fun conduct() {
        val order = speakingOrder(speakers)
        repeat(rounds) { index ->
            order.forEach { speaker ->
                val statement = speaker.discuss(speakers)
                sendStatement(index + 1, speaker.name, statement)
            }
        }
    }
}

open class OpenDiscussion(speakers: List<Player>, private val recipients: AllPlayers) : Discussion(speakers) {
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: Statement) =
        GameEvent.StatementMade.send(round, speakerName, statement, recipients)
}
