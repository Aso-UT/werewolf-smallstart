package org.example

class VillagerCpuStrategy(self: RoleAwareCpuPlayer) : RoleAwareCpuStrategy(self, Role.VILLAGER, CitizenVoting(self)) {

    override fun discuss(players: List<Player>) = Statement.Plain("")

    override fun selectTargetForOthers(context: SelectionContext, candidates: List<Player>): Player =
        candidates.random()
}
