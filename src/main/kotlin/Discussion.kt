package org.example

abstract class Discussion<R : Notifiable>(
    protected val speakers: List<Player>,
    private val recipients: R,
    protected val day: Int,
    protected val allPlayers: List<Player>,
) {
    protected abstract val rounds: Int
    protected abstract fun speakingOrder(speakers: List<Player>): List<Player>
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: R)
    protected abstract fun buildContext(round: Int): DiscussionContext

    fun conduct() {
        if (speakers.size <= 1) return
        val order = speakingOrder(speakers)
        repeat(rounds) { index ->
            order.forEach { speaker ->
                val statement = speaker.discuss(buildContext(index + 1))
                sendStatement(index + 1, speaker.name, statement, recipients)
            }
        }
    }
}

open class OpenDiscussion(playerManager: PlayerManager, day: Int) :
    Discussion<AllPlayers>(
        playerManager.players,
        AllPlayers(playerManager),
        day,
        playerManager.allPlayers,
    ) {
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: AllPlayers) =
        GameEvent.StatementMade.send(round, speakerName, statement, recipients)
    override fun buildContext(round: Int) = DiscussionContext.Open(round, day, speakers, allPlayers)
}
