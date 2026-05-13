package werewolf.cpu

import werewolf.game.DiscussionContext
import werewolf.game.Player
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

abstract class RoleAwareCpuStrategy(
    protected val self: RoleAwareCpuPlayer,
    private val targetRole: Role,
    private val votingStrategy: VotingStrategy
) {
    fun appliesTo() = self.myRole == targetRole

    abstract fun buildStatement(context: DiscussionContext): Statement

    fun selectTarget(context: SelectionContext): Player {
        val candidates = context.candidates()
        return if (context is SelectionContext.Vote)
            votingStrategy.selectVoteTarget(candidates)
        else
            selectTargetForOthers(context, candidates)
    }

    abstract fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player
}
