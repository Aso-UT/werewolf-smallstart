package org.example

abstract class Discussion(
    private val speakers: List<Player>,
    private val recipients: AllPlayers
) {
    protected abstract val rounds: Int
    protected abstract fun speakingOrder(speakers: List<Player>): List<Player>
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: AllPlayers)

    fun conduct() {
        val order = speakingOrder(speakers)
        repeat(rounds) { index ->
            order.forEach { speaker ->
                val statement = speaker.discuss(speakers)
                sendStatement(index + 1, speaker.name, statement, recipients)
            }
        }
    }
}

open class OpenDiscussion(speakers: List<Player>, recipients: AllPlayers) : Discussion(speakers, recipients) {
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: AllPlayers) =
        GameEvent.StatementMade.send(round, speakerName, statement, recipients)
}