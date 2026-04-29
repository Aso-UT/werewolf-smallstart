package org.example

class VillagerCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.VILLAGER) {

    override fun discuss(knowledge: List<GameEvent>) = Statement.Plain("")

    override fun selectTarget(context: SelectionContext, knowledge: List<GameEvent>): Player {
        val candidates = context.candidates()
        return when (context) {
            is SelectionContext.Vote -> selectVoteTarget(candidates, knowledge)
            else -> candidates.random()
        }
    }

    private fun selectVoteTarget(candidates: List<Player>, knowledge: List<GameEvent>): Player {
        val reported = reportedDivinations(knowledge)
        val reportedWolves = candidatesWith(DivineResult.WEREWOLF, reported, candidates)
        val reportedInnocent = candidatesWith(DivineResult.NOT_WEREWOLF, reported, candidates)

        if (reportedWolves.isNotEmpty()) return reportedWolves.random()
        val votable = candidates - reportedInnocent.toSet()
        return if (votable.isNotEmpty()) votable.random() else candidates.random()
    }
}
