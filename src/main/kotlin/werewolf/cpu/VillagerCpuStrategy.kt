package werewolf.cpu

import werewolf.game.DiscussionContext
import werewolf.game.Player
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

class VillagerCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.VILLAGER, CitizenVoting(self)) {

    override fun buildStatement(context: DiscussionContext) = Statement.Plain("")

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()
}
