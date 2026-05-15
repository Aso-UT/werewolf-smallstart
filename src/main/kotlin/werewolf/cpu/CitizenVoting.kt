package werewolf.cpu

import werewolf.game.DivineResult
import werewolf.game.Player

class CitizenVoting(player: RoleAwareCpuPlayer) : VotingStrategy {
    private val query = KnowledgeQuery(player)

    override fun selectVoteTarget(candidates: List<Player>): Player {
        val reportedWolves = query.reportedAs(DivineResult.WEREWOLF, candidates)
        val reportedInnocent = query.reportedAs(DivineResult.NOT_WEREWOLF, candidates)

        if (reportedWolves.isNotEmpty()) return reportedWolves.random()
        val votable = candidates - reportedInnocent.toSet()
        return if (votable.isNotEmpty()) votable.random() else candidates.random()
    }
}
