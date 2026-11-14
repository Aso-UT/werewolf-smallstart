package werewolf.game

sealed class DiscussionContext {
    abstract val round: Int
    abstract val day: Int
    abstract val players: List<Player>
    abstract val allPlayers: List<Player>
    // 直前の発言。同時発言なら、その全員の発言
    abstract val previousStatements: List<Statement>
    abstract val title: String
    abstract val description: String
    abstract val availableTypes: Set<StatementType>

    abstract fun narrowedTo(types: Set<StatementType>): DiscussionContext

    data class Open(
        override val round: Int,
        override val day: Int,
        override val players: List<Player>,
        override val allPlayers: List<Player>,
        override val previousStatements: List<Statement> = emptyList(),
        override val availableTypes: Set<StatementType> = StatementType.entries.toSet(),
    ) : DiscussionContext() {
        override val title = GameEvent.StatementMade.TITLE
        override val description = "全プレイヤーに向けた議論です。"
        override fun narrowedTo(types: Set<StatementType>) = copy(availableTypes = availableTypes intersect types)
    }

    data class Conclave(
        override val round: Int,
        override val day: Int,
        override val players: List<Player>,
        override val allPlayers: List<Player>,
        override val previousStatements: List<Statement> = emptyList(),
        override val availableTypes: Set<StatementType> = setOf(StatementType.PLAIN),
    ) : DiscussionContext() {
        override val title = GameEvent.WerewolfStatementMade.TITLE
        override val description = "この会話は人狼にしか聞こえません。"
        override fun narrowedTo(types: Set<StatementType>) = copy(availableTypes = availableTypes intersect types)
    }
}

fun DiscussionContext.selectableTypes(claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Set<StatementType> {
    val types = if (claimableRoles.isEmpty()) availableTypes - StatementType.ROLE_CLAIM else availableTypes
    return types - reportEligibility.disallowedTypes()
}
