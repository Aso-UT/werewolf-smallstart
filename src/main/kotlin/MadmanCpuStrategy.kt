package org.example

class MadmanCpuStrategy(
    self: RoleAwareCpuPlayer,
    private val impersonating: Role = listOf(Role.SEER, Role.MEDIUM).random(),
) : RoleAwareCpuStrategy(self, Role.MADMAN, WerewolfVoting(self)) {

    override fun buildStatement(context: DiscussionContext): Statement {
        if (context.round > 1) return Statement.Plain("")
        return if (impersonating == Role.SEER) fakeSeerStatement(context) else fakeMediumStatement()
    }

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()

    private fun fakeSeerStatement(context: DiscussionContext): Statement {
        val result = if (context.day == 1) DivineResult.NOT_WEREWOLF else DivineResult.WEREWOLF
        val target = context.players.filter { it !== self && it !in accusedByMe() }.randomOrNull()
            ?: return Statement.Plain("")
        return Statement.DivinationReport(self, target, result)
    }

    private fun fakeMediumStatement(): Statement {
        val target = self.knowledge.filterIsInstance<GameEvent.PlayerExecuted>()
            .map { it.executed }
            .firstOrNull { it !in fakeMediumedByMe() }
            ?: return Statement.Plain("")
        return Statement.MediumReport(self, target, MediumResult.NOT_WEREWOLF)
    }

    private fun accusedByMe(): Set<Player> =
        self.knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }
            .filterIsInstance<Statement.DivinationReport>()
            .filter { it.claimant === self }
            .map { it.target }
            .toSet()

    private fun fakeMediumedByMe(): Set<Player> =
        self.knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }
            .filterIsInstance<Statement.MediumReport>()
            .filter { it.claimant === self }
            .map { it.target }
            .toSet()
}
