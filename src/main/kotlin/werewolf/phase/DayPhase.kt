package werewolf.phase

import werewolf.game.MajorityVoteResolver
import werewolf.game.Oracle
import werewolf.game.PlayerManager
import werewolf.game.SelectionContext

class DayPhase(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val nightNumber: Int
) : Phase {
    override fun proceed(): Phase {
        OpenDiscussion(playerManager, nightNumber).conduct()
        val votes = playerManager.players.map { it.selectTarget(SelectionContext.Vote(it, playerManager.players)) }
        val mostVoted = MajorityVoteResolver.resolveNonEmpty(votes)
        playerManager.execute(mostVoted)
        return nextNight()
    }

    private fun nextNight() = NightPhase(playerManager, oracle, nightNumber + 1)
}
