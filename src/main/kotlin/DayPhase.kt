package org.example

class DayPhase(private val playerManager: PlayerManager) : Phase {
    override fun proceed() {
        RandomOrderDiscussion(playerManager.players, playerManager.allPlayers).conduct()
        val votes = playerManager.players.map { it.selectTarget(SelectionContext.Vote(it, playerManager.players)) }
        val mostVoted = MajorityVoteResolver.resolveNonEmpty(votes)
        playerManager.execute(mostVoted)
    }
}