package werewolf.phase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Oracle
import werewolf.game.PlayerManager
import werewolf.game.Statement
import werewolf.game.Wolves
import kotlin.time.Duration

open class Conclave(
    oracle: Oracle,
    playerManager: PlayerManager,
    day: Int,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
) :
    Discussion<Wolves>(
        oracle.werewolves(playerManager.players),
        Wolves(oracle, playerManager),
        day,
        playerManager.allPlayers,
        dispatcher,
    ) {
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: Wolves) =
        GameEvent.WerewolfStatementMade.send(round, speakerName, statement.text(), recipients)
    override fun buildContext(round: Int) = DiscussionContext.Conclave(round, day, speakers, allPlayers)
    override fun sendDiscussionStarted() = GameEvent.ConclaveStarted.send(day, recipients)
    override fun sendDiscussionEnded(elapsed: Duration) = GameEvent.ConclaveEnded.send(day, elapsed, recipients)
}
