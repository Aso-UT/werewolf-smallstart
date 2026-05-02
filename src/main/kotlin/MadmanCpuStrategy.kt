package org.example

class MadmanCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.MADMAN, CitizenVoting(self)) {
    override fun discuss() = Statement.Plain("")
    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player = candidates.random()
}
