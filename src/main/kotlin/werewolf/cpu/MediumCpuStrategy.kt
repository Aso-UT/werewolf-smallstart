package werewolf.cpu

import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Player
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

class MediumCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.MEDIUM, CitizenVoting(self)) {

    override fun buildStatement(context: DiscussionContext): Statement {
        val next = nextUnreportedReveal() ?: return Statement.Plain("")
        return Statement.MediumReport(self, next.target, next.result)
    }

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()

    private fun nextUnreportedReveal(): GameEvent.MediumRevealed? {
        val reported = reportedTargets()
        return self.knowledge.filterIsInstance<GameEvent.MediumRevealed>().firstOrNull { it.target !in reported }
    }

    private fun reportedTargets(): Set<Player> =
        self.knowledge.filterIsInstance<GameEvent.StatementMade>()
            .map { it.statement }.filterIsInstance<Statement.MediumReport>()
            .filter { it.claimant === self }.map { it.target }.toSet()
}
