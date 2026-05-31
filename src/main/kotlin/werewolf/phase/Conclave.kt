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
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: Wolves) =
        GameEvent.WerewolfStatementMade.send(round, speakerName, statement.text(), recipients)
    override fun buildContext(round: Int) = DiscussionContext.Conclave(round, day, speakers, allPlayers)
    override fun sendDiscussionStarted() = GameEvent.ConclaveStarted.send(day, recipients)
}
