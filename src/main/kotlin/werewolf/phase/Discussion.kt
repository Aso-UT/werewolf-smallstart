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
) {
    protected abstract fun shouldEnd(history: SpeakingHistory): Boolean
    // contexts: 各発言者と、その人が次に発言する場合のコンテキスト
    protected abstract fun nextSpeaker(contexts: Map<Player, DiscussionContext>): Player
    protected abstract fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: R)
    protected abstract fun buildContext(round: Int): DiscussionContext
    protected abstract fun sendDiscussionStarted()

    fun conduct() {
        if (speakers.size <= 1) return
        sendDiscussionStarted()
        val history = SpeakingHistory()
        while (!shouldEnd(history)) {
            val contexts = nextContexts(history)
            val speaker = nextSpeaker(contexts)
            val context = contexts.getValue(speaker)
            val statement = speaker.discuss(context)
            sendStatement(context.round, speaker.name, statement, recipients)
            history.record(speaker)
        }
    }

    private fun nextContexts(history: SpeakingHistory): Map<Player, DiscussionContext> =
        speakers.associateWith { buildContext(roundOf(it, history)) }

    // そのプレイヤーにとって何回目の発言か
    private fun roundOf(player: Player, history: SpeakingHistory): Int = history.countOf(player) + 1
}

open class OpenDiscussion(
    playerManager: PlayerManager,
    day: Int,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) :
    Discussion<AllPlayers>(
        playerManager.players,
        AllPlayers(playerManager),
        day,
        playerManager.allPlayers,
    ) {
    override fun shouldEnd(history: SpeakingHistory) = history.totalCount >= speakers.size * STATEMENTS_PER_PLAYER

    override fun nextSpeaker(contexts: Map<Player, DiscussionContext>): Player {
        val eagerness = collectEagerness(contexts)
        val highest = eagerness.values.max()
        return eagerness.filterValues { it == highest }.keys.random()
    }

    private fun collectEagerness(contexts: Map<Player, DiscussionContext>): Map<Player, Eagerness> = runBlocking {
        contexts.map { (player, context) ->
            async(dispatcher) { player to player.eagerness(context) }
        }.awaitAll().toMap()
    }

    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: AllPlayers) =
        GameEvent.StatementMade.send(round, speakerName, statement, recipients)
    override fun buildContext(round: Int) = DiscussionContext.Open(round, day, speakers, allPlayers)
    override fun sendDiscussionStarted() = GameEvent.DiscussionStarted.send(day, recipients)

    companion object {
        // 総発言回数が「人数×この値」に達したら議論を打ち切る
        private const val STATEMENTS_PER_PLAYER = 3
    }
}
