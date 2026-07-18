package werewolf.cpu

import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.selectableTypes

class MadmanCpuStrategy(
    self: RoleAwareCpuPlayer,
    private val impersonating: Role = listOf(Role.SEER, Role.MEDIUM).random(),
) : RoleAwareCpuStrategy(self, Role.MADMAN, WerewolfVoting(self)) {

    override fun buildStatement(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Statement {
        if (context.round > 1) return Statement.Plain(self, "")
        val impersonatedType = if (impersonating == Role.SEER) StatementType.DIVINATION_REPORT else StatementType.MEDIUM_REPORT
        if (impersonatedType !in context.selectableTypes(claimableRoles, reportEligibility)) return Statement.Plain(self, "")
        return if (impersonating == Role.SEER) fakeSeerStatement(context) else fakeMediumStatement()
    }

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()

    private fun fakeSeerStatement(context: DiscussionContext): Statement {
        val result = if (context.day == 1) DivineResult.NOT_WEREWOLF else DivineResult.WEREWOLF
        val target = context.players.filter { it !== self && it !in accusedByMe() }.randomOrNull()
            ?: return Statement.Plain(self, "")
        return Statement.DivinationReport(self, target, result)
    }

    private fun fakeMediumStatement(): Statement {
        val target = self.knowledge.filterIsInstance<GameEvent.PlayerExecuted>()
            .map { it.executed }
            .firstOrNull { it !in fakeMediumedByMe() }
            ?: return Statement.Plain(self, "")
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
