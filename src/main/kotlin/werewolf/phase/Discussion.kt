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
import werewolf.game.Turn
import werewolf.util.Cardinality
import werewolf.util.cardinality
import kotlin.time.Duration
import kotlin.time.measureTime

abstract class Discussion<R : Notifiable>(
    protected val speakers: List<Player>,
    protected val recipients: R,
    protected val day: Int,
    protected val allPlayers: List<Player>,
    private val dispatcher: CoroutineDispatcher,
) {
    private val speakingRightGrant = SpeakingRightGrant(dispatcher)

    protected abstract fun sendStatements(turn: Turn, recipients: R)
    protected abstract fun buildContext(round: Int, lastTurn: Turn?): DiscussionContext
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
            val rights = when (val eligible = contexts.keys.cardinality()) {
                Cardinality.None -> return
                is Cardinality.One -> listOf(SpeakingRight.Uncontested(eligible.value))
                is Cardinality.Many -> speakingRightGrant.contest(contexts)
            }
            val turn = speak(rights, contexts)
            sendStatements(turn, recipients)
            history.record(turn)
        }
    }

    // 発言権を得た人が複数いれば、互いの発言を知らないまま並列に発言する
    private fun speak(rights: List<SpeakingRight>, contexts: Map<Player, DiscussionContext>): Turn {
        val rightOf = rights.associateBy { it.speaker }
        return Turn(rightOf.keys.askInParallel(dispatcher) { it.discuss(contexts.getValue(it), rightOf.getValue(it)) }.values.toList())
    }

    private fun reachedTotalLimit(history: SpeakingHistory): Boolean =
        history.totalCount >= speakers.size * AVERAGE_STATEMENTS_PER_SPEAKER

    private fun eligibleSpeakerContexts(history: SpeakingHistory): Map<Player, DiscussionContext> =
        speakers.filter { canSpeak(it, history) }.associateWith { buildContext(history.nextRoundOf(it), history.lastTurn) }

    private fun canSpeak(player: Player, history: SpeakingHistory): Boolean =
        !reachedPersonalLimit(player, history) && !history.isLastTurnSpeaker(player)

    private fun reachedPersonalLimit(player: Player, history: SpeakingHistory): Boolean =
        history.countOf(player) >= MAX_STATEMENTS_PER_SPEAKER

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
    override fun sendStatements(turn: Turn, recipients: AllPlayers) =
        GameEvent.StatementMade.send(turn, recipients)
    override fun buildContext(round: Int, lastTurn: Turn?) =
        DiscussionContext.Open(round, day, speakers, allPlayers, lastTurn)
    override fun sendDiscussionStarted() = GameEvent.DiscussionStarted.send(day, recipients)
    override fun sendDiscussionEnded(elapsed: Duration) = GameEvent.DiscussionEnded.send(day, elapsed, recipients)
}
