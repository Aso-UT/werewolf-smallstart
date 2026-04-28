package org.example

abstract class Discussion<R : Notifiable>(
    private val speakers: List<Player>,
    private val recipients: R
) {
    protected abstract val rounds: Int
    protected abstract fun speakingOrder(speakers: List<Player>): List<Player>
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: R)

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

open class OpenDiscussion(speakers: List<Player>, allPlayers: AllPlayers) : Discussion<AllPlayers>(speakers, allPlayers) {
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: AllPlayers) =
        GameEvent.StatementMade.send(round, speakerName, statement, recipients)
}
