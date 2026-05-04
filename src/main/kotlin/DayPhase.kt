package org.example

class DayPhase(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val nightNumber: Int
) : Phase {
    override fun proceed(): Phase {
        OpenDiscussion(playerManager.players, AllPlayers(playerManager)).conduct()
        val votes = playerManager.players.map { it.selectTarget(SelectionContext.Vote(it, playerManager.players)) }
        val mostVoted = MajorityVoteResolver.resolveNonEmpty(votes)
        playerManager.execute(mostVoted)
        return nextNight()
    }

    private fun nextNight() = NightPhase(playerManager, oracle, nightNumber + 1)
}
