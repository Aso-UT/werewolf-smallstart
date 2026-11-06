package werewolf.phase

import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Oracle
import werewolf.game.Player
import werewolf.game.PlayerManager
import werewolf.game.Statement
import werewolf.game.Wolves

open class Conclave(oracle: Oracle, playerManager: PlayerManager, day: Int) :
    Discussion<Wolves>(
        oracle.werewolves(playerManager.players),
        Wolves(oracle, playerManager),
        day,
        playerManager.allPlayers,
    ) {
    private val order: List<Player> by lazy { speakingOrder(speakers) }

    protected open fun speakingOrder(speakers: List<Player>) = speakers.shuffled()

    override fun shouldEnd(history: SpeakingHistory) = history.totalCount >= speakers.size * ROUNDS
    // 発言回数が最も少ない人のうち、順番が先の人。結果として order の順に1回ずつ回る
    override fun nextSpeaker(contexts: Map<Player, DiscussionContext>) = order.minBy { contexts.getValue(it).round }
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: Wolves) =
        GameEvent.WerewolfStatementMade.send(round, speakerName, statement.text(), recipients)
    override fun buildContext(round: Int) = DiscussionContext.Conclave(round, day, speakers, allPlayers)
    override fun sendDiscussionStarted() = GameEvent.ConclaveStarted.send(day, recipients)

    companion object {
        private const val ROUNDS = 3
    }
}
