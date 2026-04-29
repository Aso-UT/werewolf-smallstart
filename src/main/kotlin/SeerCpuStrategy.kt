package org.example

class SeerCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.SEER) {

    override fun discuss(knowledge: List<GameEvent>): Statement {
        val next = nextUnreportedDivination(knowledge) ?: return Statement.Plain("")
        return Statement.DivinationReport(self, next.target, next.result)
    }

    override fun selectTarget(context: SelectionContext, knowledge: List<GameEvent>): Player {
        val candidates = context.candidates()
        return when (context) {
            is SelectionContext.Divine -> selectDivineTarget(candidates, knowledge)
            is SelectionContext.Vote -> selectVoteTarget(candidates, knowledge)
            else -> candidates.random()
        }
    }

    private fun nextUnreportedDivination(knowledge: List<GameEvent>): GameEvent.Divined? {
        val reported = reportedTargets(knowledge)
        return knowledge.filterIsInstance<GameEvent.Divined>().firstOrNull { it.target !in reported }
    }

    private fun reportedTargets(knowledge: List<GameEvent>): Set<Player> =
        knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.DivinationReport>()
            .filter { it.claimant === self }.map { it.target }.toSet()

    private fun selectDivineTarget(candidates: List<Player>, knowledge: List<GameEvent>): Player {
        val divined = knowledge.filterIsInstance<GameEvent.Divined>().map { it.target }.toSet()
        val undivined = candidates.filter { it !in divined }
        return if (undivined.isNotEmpty()) undivined.random() else candidates.random()
    }

    private fun selectVoteTarget(candidates: List<Player>, knowledge: List<GameEvent>): Player {
        val myDivined = myDivinedResults(knowledge)
        val reported = reportedDivinations(knowledge)

        val myWolves = candidatesWith(DivineResult.WEREWOLF, myDivined, candidates)
        val reportedWolves = candidatesWith(DivineResult.WEREWOLF, reported, candidates)
        val myInnocent = candidatesWith(DivineResult.NOT_WEREWOLF, myDivined, candidates)
        val reportedInnocent = candidatesWith(DivineResult.NOT_WEREWOLF, reported, candidates)

        if (myWolves.isNotEmpty()) return myWolves.random()
        val suspectedWolves = reportedWolves - myInnocent.toSet()
        if (suspectedWolves.isNotEmpty()) return suspectedWolves.random()
        val votable = candidates - myInnocent.toSet() - reportedInnocent.toSet()
        return if (votable.isNotEmpty()) votable.random() else candidates.random()
    }

    private fun myDivinedResults(knowledge: List<GameEvent>): Map<Player, DivineResult> =
        knowledge.filterIsInstance<GameEvent.Divined>().associate { it.target to it.result }

}
