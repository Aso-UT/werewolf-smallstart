package org.example

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
