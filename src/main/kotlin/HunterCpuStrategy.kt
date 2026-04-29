package org.example

class HunterCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.HUNTER, CitizenVoting(self)) {
    private val query = KnowledgeQuery(self)

    override fun discuss() = Statement.Plain("")

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        when (context) {
            is SelectionContext.Guard -> selectGuardTarget(candidates)
            else -> candidates.random()
        }

    private fun selectGuardTarget(candidates: List<Player>): Player {
        val targets = candidates.filter { it in query.claimedSeers() }
        return if (targets.isNotEmpty()) targets.random() else candidates.random()
    }
}
