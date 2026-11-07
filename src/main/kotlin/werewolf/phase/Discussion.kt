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
            val contexts = nextContexts(history)
            if (contexts.isEmpty()) return
            val speaker = mostEager(contexts)
            val context = contexts.getValue(speaker)
            val statement = speaker.discuss(context)
            sendStatement(context.round, speaker.name, statement, recipients)
            history.record(speaker)
        }
    }

    private fun reachedTotalLimit(history: SpeakingHistory): Boolean =
        history.totalCount >= speakers.size * AVERAGE_STATEMENTS_PER_SPEAKER

    // 発言できる人と、その人が次に発言する場合のコンテキスト
    private fun nextContexts(history: SpeakingHistory): Map<Player, DiscussionContext> =
        speakers.filter { canSpeak(it, history) }.associateWith { buildContext(roundOf(it, history)) }

    // 発言回数の上限に達しておらず、直前の発言者でもない
    private fun canSpeak(player: Player, history: SpeakingHistory): Boolean =
        history.countOf(player) < MAX_STATEMENTS_PER_SPEAKER && history.lastSpeaker !== player

    // そのプレイヤーにとって何回目の発言か
    private fun roundOf(player: Player, history: SpeakingHistory): Int = history.countOf(player) + 1

    // 発言意欲が最も高い人。同点ならランダム
    private fun mostEager(contexts: Map<Player, DiscussionContext>): Player {
        val eagerness = collectEagerness(contexts)
        val highest = eagerness.values.max()
        return eagerness.filterValues { it == highest }.keys.random()
    }

    private fun collectEagerness(contexts: Map<Player, DiscussionContext>): Map<Player, Eagerness> = runBlocking {
        contexts.map { (player, context) ->
            async(dispatcher) { player to player.eagerness(context) }
        }.awaitAll().toMap()
    }

    companion object {
        // 一人がひとつの議論で発言できる最大回数
        private const val MAX_STATEMENTS_PER_SPEAKER = 4
        // 総発言回数が「発言者数×この値」に達したら議論を打ち切る
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
