package org.example

abstract class RoleAwareCpuStrategy(
    protected val self: RoleAwareCpuPlayer,
    private val targetRole: Role
) {
    fun appliesTo() = self.myRole == targetRole
    abstract fun discuss(knowledge: List<GameEvent>): Statement
    abstract fun selectTarget(context: SelectionContext, knowledge: List<GameEvent>): Player

    protected fun claimedSeers(knowledge: List<GameEvent>): Set<Player> =
        knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.DivinationReport>()
            .map { it.claimant }.toSet()

    protected fun reportedDivinations(knowledge: List<GameEvent>): Map<Player, DivineResult> =
        knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.DivinationReport>()
            .associate { it.target to it.result }

    protected fun candidatesWith(
        result: DivineResult,
        from: Map<Player, DivineResult>,
        candidates: List<Player>
    ): List<Player> = candidates.filter { from[it] == result }
}
