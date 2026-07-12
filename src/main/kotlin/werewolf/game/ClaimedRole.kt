package werewolf.game

class ClaimedRole(memories: List<Recallable>) {
    private val statements = memories.filterIsInstance<GameEvent.StatementMade>().map { it.statement }

    // 複数回申告された場合は最後の申告を優先する（人狼・狂人の騙り替えを想定）
    @Suppress("FunctionMinLength")
    fun of(player: Player): Role? = statements.mapNotNull { roleFrom(it, player) }.lastOrNull()

    private fun roleFrom(statement: Statement, player: Player): Role? = when {
        statement is Statement.RoleClaim && statement.claimant === player -> statement.role
        statement is Statement.DivinationReport && statement.claimant === player -> Role.SEER
        statement is Statement.MediumReport && statement.claimant === player -> Role.MEDIUM
        else -> null
    }
}
