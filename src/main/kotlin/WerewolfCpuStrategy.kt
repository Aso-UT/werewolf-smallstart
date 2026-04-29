package org.example

class WerewolfCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.WEREWOLF) {

    override fun discuss(knowledge: List<GameEvent>) = Statement.Plain("")

    override fun selectTarget(context: SelectionContext, knowledge: List<GameEvent>): Player {
        val candidates = context.candidates()
        return when (context) {
            is SelectionContext.Attack -> selectAttackTarget(candidates, knowledge)
            is SelectionContext.Vote -> selectVoteTarget(candidates, knowledge)
            else -> candidates.random()
        }
    }

    private fun selectAttackTarget(candidates: List<Player>, knowledge: List<GameEvent>): Player {
        val targets = candidates.filter { it in claimedSeers(knowledge) }
        return if (targets.isNotEmpty()) targets.random() else candidates.random()
    }

    private fun selectVoteTarget(candidates: List<Player>, knowledge: List<GameEvent>): Player {
        val targets = candidates.filter { it in claimedSeers(knowledge) }
        return if (targets.isNotEmpty()) targets.random() else candidates.random()
    }
}
