package werewolf.cpu

import werewolf.game.DiscussionContext
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.selectableTypes

class MediumCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.MEDIUM, CitizenVoting(self)) {

    override fun buildStatement(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Statement {
        if (StatementType.MEDIUM_REPORT !in context.selectableTypes(claimableRoles, reportEligibility)) return Statement.Plain(self, "")
        val (target, result) = (reportEligibility as ReportEligibility.Honest).mediums.entries.first()
        return Statement.MediumReport(self, target, result)
    }

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()
}
