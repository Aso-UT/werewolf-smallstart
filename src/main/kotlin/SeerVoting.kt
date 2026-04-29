package org.example

class SeerVoting(player: RoleAwareCpuPlayer) : VotingStrategy {
    private val query = KnowledgeQuery(player)

    override fun selectVoteTarget(candidates: List<Player>): Player {
        val myWolves = query.divinedAs(DivineResult.WEREWOLF, candidates)
        val reportedWolves = query.reportedAs(DivineResult.WEREWOLF, candidates)
        val myInnocent = query.divinedAs(DivineResult.NOT_WEREWOLF, candidates)
        val reportedInnocent = query.reportedAs(DivineResult.NOT_WEREWOLF, candidates)

        if (myWolves.isNotEmpty()) return myWolves.random()
        val suspectedWolves = reportedWolves - myInnocent.toSet()
        if (suspectedWolves.isNotEmpty()) return suspectedWolves.random()
        val votable = candidates - myInnocent.toSet() - reportedInnocent.toSet()
        return if (votable.isNotEmpty()) votable.random() else candidates.random()
    }
}
