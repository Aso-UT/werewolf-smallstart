package org.example

class MediumCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.MEDIUM) {

    override fun discuss(knowledge: List<GameEvent>): Statement {
        val next = nextUnreportedReveal(knowledge) ?: return Statement.Plain("")
        return Statement.MediumReport(self, next.target, next.result)
    }

    override fun selectTarget(context: SelectionContext, knowledge: List<GameEvent>): Player {
        val candidates = context.candidates()
        return when (context) {
            is SelectionContext.Vote -> selectVoteTarget(candidates, knowledge)
            else -> candidates.random()
        }
    }

    private fun nextUnreportedReveal(knowledge: List<GameEvent>): GameEvent.MediumRevealed? {
        val reported = reportedTargets(knowledge)
        return knowledge.filterIsInstance<GameEvent.MediumRevealed>().firstOrNull { it.target !in reported }
    }

    private fun reportedTargets(knowledge: List<GameEvent>): Set<Player> =
        knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.MediumReport>()
            .filter { it.claimant === self }.map { it.target }.toSet()

    private fun selectVoteTarget(candidates: List<Player>, knowledge: List<GameEvent>): Player {
        val reported = reportedDivinations(knowledge)

        val reportedWolves = candidatesWith(DivineResult.WEREWOLF, reported, candidates)
        val reportedInnocent = candidatesWith(DivineResult.NOT_WEREWOLF, reported, candidates)

        if (reportedWolves.isNotEmpty()) return reportedWolves.random()
        val votable = candidates - reportedInnocent.toSet()
        return if (votable.isNotEmpty()) votable.random() else candidates.random()
    }
}
