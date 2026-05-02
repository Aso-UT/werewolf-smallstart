package org.example

class SeerCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.SEER, SeerVoting(self)) {

    override fun discuss(players: List<Player>): Statement {
        val next = nextUnreportedDivination() ?: return Statement.Plain("")
        return Statement.DivinationReport(self, next.target, next.result)
    }

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        when (context) {
            is SelectionContext.Divine -> selectDivineTarget(candidates)
            else -> candidates.random()
        }

    private fun nextUnreportedDivination(): GameEvent.Divined? {
        val reported = reportedTargets()
        return self.knowledge.filterIsInstance<GameEvent.Divined>().firstOrNull { it.target !in reported }
    }

    private fun reportedTargets(): Set<Player> =
        self.knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.DivinationReport>()
            .filter { it.claimant === self }.map { it.target }.toSet()

    private fun selectDivineTarget(candidates: List<Player>): Player {
        val divined = self.knowledge.filterIsInstance<GameEvent.Divined>().map { it.target }.toSet()
        val undivined = candidates.filter { it !in divined }
        return if (undivined.isNotEmpty()) undivined.random() else candidates.random()
    }
}
