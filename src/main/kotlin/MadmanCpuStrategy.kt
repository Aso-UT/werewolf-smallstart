package org.example

class MadmanCpuStrategy(
    self: RoleAwareCpuPlayer,
    private val impersonating: Role = listOf(Role.SEER, Role.MEDIUM).random(),
) : RoleAwareCpuStrategy(self, Role.MADMAN, WerewolfVoting(self)) {

    override fun discuss(players: List<Player>): Statement =
        if (impersonating == Role.SEER) fakeSeerDiscuss(players) else fakeMediumDiscuss()

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()

    private fun fakeSeerDiscuss(players: List<Player>): Statement {
        val target = players.filter { it !== self && it !in accusedByMe() }.randomOrNull()
            ?: return Statement.Plain("")
        return Statement.DivinationReport(self, target, DivineResult.WEREWOLF)
    }

    private fun fakeMediumDiscuss(): Statement {
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
