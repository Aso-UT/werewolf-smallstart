package werewolf.game

class UnreportedResults(memories: List<Recallable>, self: Player) {
    private val divined = memories.filterIsInstance<GameEvent.Divined>().associate { it.target to it.result }
    private val mediumRevealed = memories.filterIsInstance<GameEvent.MediumRevealed>().associate { it.target to it.result }
    private val ownStatements = memories.filterIsInstance<GameEvent.StatementMade>()
        .map { it.statement }.filter { it.claimant === self }

    val divinations: Map<Player, DivineResult>
        get() = divined - ownDivinationReports().keys

    val mediums: Map<Player, MediumResult>
        get() = mediumRevealed - ownMediumReports().keys

    private fun ownDivinationReports(): Map<Player, DivineResult> =
        ownStatements.filterIsInstance<Statement.DivinationReport>().associate { it.target to it.result }

    private fun ownMediumReports(): Map<Player, MediumResult> =
        ownStatements.filterIsInstance<Statement.MediumReport>().associate { it.target to it.result }
}
