package werewolf.phase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import werewolf.game.AllPlayers
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.GameEvent
import werewolf.game.Notifiable
import werewolf.game.Player
import werewolf.game.PlayerManager
import werewolf.game.Statement

abstract class Discussion<R : Notifiable>(
    protected val speakers: List<Player>,
    protected val recipients: R,
    protected val day: Int,
    protected val allPlayers: List<Player>,
    private val dispatcher: CoroutineDispatcher,
) {
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: R)
    protected abstract fun buildContext(round: Int): DiscussionContext
    protected abstract fun sendDiscussionStarted()

    fun conduct() {
        if (speakers.size <= 1) return
        sendDiscussionStarted()
        val history = SpeakingHistory()
        while (!reachedTotalLimit(history)) {
            val contexts = eligibleSpeakerContexts(history)
            if (contexts.isEmpty()) return
            val speaker = nextSpeaker(contexts)
            val context = contexts.getValue(speaker)
            val statement = speaker.discuss(context)
            sendStatement(context.round, speaker.name, statement, recipients)
            history.record(speaker)
        }
    }

    private fun reachedTotalLimit(history: SpeakingHistory): Boolean =
        history.totalCount >= speakers.size * AVERAGE_STATEMENTS_PER_SPEAKER

    private fun eligibleSpeakerContexts(history: SpeakingHistory): Map<Player, DiscussionContext> =
        speakers.filter { canSpeak(it, history) }.associateWith { buildContext(history.nextRoundOf(it)) }

    private fun canSpeak(player: Player, history: SpeakingHistory): Boolean =
        !reachedPersonalLimit(player, history) && !history.isLastSpeaker(player)

    private fun reachedPersonalLimit(player: Player, history: SpeakingHistory): Boolean =
        history.countOf(player) >= MAX_STATEMENTS_PER_SPEAKER

    private fun nextSpeaker(contexts: Map<Player, DiscussionContext>): Player =
        contexts.keys.singleOrNull() ?: mostEager(contexts)

    private fun mostEager(contexts: Map<Player, DiscussionContext>): Player =
        playersWithHighestEagerness(collectEagerness(contexts)).random()

    private fun playersWithHighestEagerness(eagerness: Map<Player, Eagerness>): Set<Player> {
        val highest = eagerness.values.max()
        return eagerness.filterValues { it == highest }.keys
    }

    private fun collectEagerness(contexts: Map<Player, DiscussionContext>): Map<Player, Eagerness> = runBlocking {
        contexts.map { (player, context) ->
            async(dispatcher) { player to player.eagerness(context) }
        }.awaitAll().toMap()
    }

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
    override fun buildContext(round: Int) = DiscussionContext.Open(round, day, speakers, allPlayers)
    override fun sendDiscussionStarted() = GameEvent.DiscussionStarted.send(day, recipients)
}
