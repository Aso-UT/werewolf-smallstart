package org.example

class MadmanCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.MADMAN, WerewolfVoting(self)) {

    override fun discuss(players: List<Player>): Statement {
        val target = nextUnreportedTarget(players) ?: return Statement.Plain("")
        return Statement.DivinationReport(self, target, DivineResult.WEREWOLF)
    }

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()

    private fun nextUnreportedTarget(players: List<Player>): Player? {
        val accused = accusedByMe()
        return players.filter { it !== self && it !in accused }.randomOrNull()
    }

    private fun accusedByMe(): Set<Player> =
        self.knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }
            .filterIsInstance<Statement.DivinationReport>()
            .filter { it.claimant === self }
            .map { it.target }
            .toSet()
}

