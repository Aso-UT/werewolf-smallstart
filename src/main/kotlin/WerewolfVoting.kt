package org.example

class WerewolfVoting(player: RoleAwareCpuPlayer) : VotingStrategy {
    private val query = KnowledgeQuery(player)

    override fun selectVoteTarget(candidates: List<Player>): Player {
        val targets = candidates.filter { it in query.claimedSeers() }
        return if (targets.isNotEmpty()) targets.random() else candidates.random()
    }
}
