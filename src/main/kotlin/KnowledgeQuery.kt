package org.example

class KnowledgeQuery(private val player: RoleAwareCpuPlayer) {
    private val knowledge get() = player.knowledge
    private val self: Player get() = player

    fun divinedAs(result: DivineResult, candidates: List<Player>): List<Player> {
        val myDivined = knowledge.filterIsInstance<GameEvent.Divined>().associate { it.target to it.result }
        return candidates.filter { myDivined[it] == result }
    }

    fun reportedAs(result: DivineResult, candidates: List<Player>): List<Player> {
        val others = knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.DivinationReport>()
            .filter { it.claimant !== self }
            .associate { it.target to it.result }
        return candidates.filter { others[it] == result }
    }

    fun claimedSeers(): Set<Player> =
        knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.DivinationReport>()
            .filter { it.claimant !== self }
            .map { it.claimant }.toSet()
}
