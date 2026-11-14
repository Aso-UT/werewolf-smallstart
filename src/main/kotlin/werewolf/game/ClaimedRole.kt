package werewolf.game

class ClaimedRole(memories: List<Recallable>) {
    private val statements = memories.filterIsInstance<GameEvent.StatementMade>().flatMap { it.statements }

    // 複数回申告された場合は最後の申告を優先する（人狼・狂人の騙り替えを想定）
    @Suppress("FunctionMinLength")
    fun of(player: Player): Role? = statements.filter { it.claimant === player }.mapNotNull { it.claimedRole }.lastOrNull()
}
