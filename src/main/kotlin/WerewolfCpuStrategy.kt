package org.example

class WerewolfCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.WEREWOLF, WerewolfVoting(self)) {
    private val query = KnowledgeQuery(self)

    override fun buildStatement(context: DiscussionContext) = Statement.Plain("")

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        when (context) {
            is SelectionContext.Attack -> selectAttackTarget(candidates)
            else -> candidates.random()
        }

    private fun selectAttackTarget(candidates: List<Player>): Player {
        val targets = candidates.filter { it in query.claimedSeers() }
        return if (targets.isNotEmpty()) targets.random() else candidates.random()
    }
}
