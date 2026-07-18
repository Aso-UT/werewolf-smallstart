package werewolf.game

sealed class ReportEligibility {
    abstract fun disallowedTypes(): Set<StatementType>
    abstract fun disqualifies(statement: Statement): Boolean
    abstract fun divinationCandidates(): List<Player>
    abstract fun mediumCandidates(): List<Player>
    abstract fun divinationResults(target: Player): List<DivineResult>
    abstract fun mediumResults(target: Player): List<MediumResult>

    data class Honest(
        val divinations: Map<Player, DivineResult> = emptyMap(),
        val mediums: Map<Player, MediumResult> = emptyMap(),
    ) : ReportEligibility() {
        override fun disallowedTypes(): Set<StatementType> = buildSet {
            if (divinations.isEmpty()) add(StatementType.DIVINATION_REPORT)
            if (mediums.isEmpty()) add(StatementType.MEDIUM_REPORT)
        }

        override fun disqualifies(statement: Statement): Boolean = when (statement) {
            is Statement.DivinationReport -> divinations[statement.target] != statement.result
            is Statement.MediumReport -> mediums[statement.target] != statement.result
            else -> false
        }

        override fun divinationCandidates(): List<Player> = divinations.keys.toList()
        override fun mediumCandidates(): List<Player> = mediums.keys.toList()
        override fun divinationResults(target: Player): List<DivineResult> = listOf(divinations.getValue(target))
        override fun mediumResults(target: Player): List<MediumResult> = listOf(mediums.getValue(target))
    }

    data class CanLie(private val otherPlayers: List<Player>) : ReportEligibility() {
        override fun disallowedTypes(): Set<StatementType> = emptySet()
        override fun disqualifies(statement: Statement): Boolean = false
        override fun divinationCandidates(): List<Player> = otherPlayers
        override fun mediumCandidates(): List<Player> = otherPlayers
        override fun divinationResults(target: Player): List<DivineResult> = DivineResult.entries
        override fun mediumResults(target: Player): List<MediumResult> = MediumResult.entries
    }

    companion object {
        fun forRole(role: Role, memories: List<Recallable>, self: Player): ReportEligibility {
            val unreported = UnreportedResults(memories, self)
            return when (role) {
                Role.SEER -> Honest(divinations = unreported.divinations)
                Role.MEDIUM -> Honest(mediums = unreported.mediums)
                Role.WEREWOLF, Role.MADMAN -> CanLie(otherPlayers(memories, self))
                Role.VILLAGER, Role.HUNTER -> Honest()
            }
        }

        private fun otherPlayers(memories: List<Recallable>, self: Player): List<Player> =
            memories.filterIsInstance<GameEvent.PlayersAnnounced>().firstOrNull()?.players.orEmpty().filter { it !== self }
    }
}
