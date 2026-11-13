package werewolf.phase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import werewolf.game.AllPlayers
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Notifiable
import werewolf.game.Player
import werewolf.game.PlayerManager
import werewolf.game.SpeakingRight
import werewolf.game.SpeakingUrge
import werewolf.game.Statement
import kotlin.time.Duration
import kotlin.time.measureTime

abstract class Discussion<R : Notifiable>(
    protected val speakers: List<Player>,
    protected val recipients: R,
    protected val day: Int,
    protected val allPlayers: List<Player>,
    private val dispatcher: CoroutineDispatcher,
) {
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: R)
    protected abstract fun buildContext(round: Int, previousStatement: Statement?): DiscussionContext
    protected abstract fun sendDiscussionStarted()
    protected abstract fun sendDiscussionEnded(elapsed: Duration)

    fun conduct() {
        if (speakers.size <= 1) return
        sendDiscussionStarted()
        val elapsed = measureTime { exchangeStatements() }
        sendDiscussionEnded(elapsed)
    }

    private fun exchangeStatements() {
        val history = SpeakingHistory()
        while (!reachedTotalLimit(history)) {
            val contexts = eligibleSpeakerContexts(history)
            if (contexts.isEmpty()) return
            val right = grantSpeakingRight(contexts)
            val context = contexts.getValue(right.speaker)
            val statement = right.speaker.discuss(context, right)
            sendStatement(context.round, right.speaker.name, statement, recipients)
            history.record(statement)
        }
    }

    private fun reachedTotalLimit(history: SpeakingHistory): Boolean =
        history.totalCount >= speakers.size * AVERAGE_STATEMENTS_PER_SPEAKER

    private fun eligibleSpeakerContexts(history: SpeakingHistory): Map<Player, DiscussionContext> =
        speakers.filter { canSpeak(it, history) }.associateWith { buildContext(history.nextRoundOf(it), history.lastStatement) }

    private fun canSpeak(player: Player, history: SpeakingHistory): Boolean =
        !reachedPersonalLimit(player, history) && !history.isLastSpeaker(player)

    private fun reachedPersonalLimit(player: Player, history: SpeakingHistory): Boolean =
        history.countOf(player) >= MAX_STATEMENTS_PER_SPEAKER

    private fun grantSpeakingRight(contexts: Map<Player, DiscussionContext>): SpeakingRight =
        contexts.keys.singleOrNull()?.let { SpeakingRight.Uncontested(it) }
            ?: SpeakingRight.Won(mostEagerUrges(collectUrges(contexts)).random())

    private fun mostEagerUrges(urges: Collection<SpeakingUrge>): List<SpeakingUrge> {
        val highest = urges.maxOf { it.eagerness }
        return urges.filter { it.eagerness == highest }
    }

    private fun collectUrges(contexts: Map<Player, DiscussionContext>): Collection<SpeakingUrge> =
        contexts.keys.askInParallel(dispatcher) { it.speakingUrge(contexts.getValue(it)) }.values

    companion object {
        private const val MAX_STATEMENTS_PER_SPEAKER = 4
        private const val AVERAGE_STATEMENTS_PER_SPEAKER = 3
    }
}

open class OpenDiscussion(
    playerManager: PlayerManager,
    day: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) :
    Discussion<AllPlayers>(
        playerManager.players,
        AllPlayers(playerManager),
        day,
        playerManager.allPlayers,
        dispatcher,
    ) {
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: AllPlayers) =
        GameEvent.StatementMade.send(round, speakerName, statement, recipients)
    override fun buildContext(round: Int, previousStatement: Statement?) =
        DiscussionContext.Open(round, day, speakers, allPlayers, previousStatement)
    override fun sendDiscussionStarted() = GameEvent.DiscussionStarted.send(day, recipients)
    override fun sendDiscussionEnded(elapsed: Duration) = GameEvent.DiscussionEnded.send(day, elapsed, recipients)
}
