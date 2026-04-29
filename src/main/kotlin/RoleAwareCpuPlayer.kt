package org.example

class RoleAwareCpuPlayer(val myRole: Role, override val name: String) : Player(myRole) {
    private val knowledge = mutableListOf<GameEvent>()
    private val strategy = setOf(
        SeerCpuStrategy(this), MediumCpuStrategy(this), WerewolfCpuStrategy(this),
        HunterCpuStrategy(this), VillagerCpuStrategy(this)
    ).single { it.appliesTo() }

    override fun onReceive(event: GameEvent) { knowledge.add(event) }
    override fun discuss(players: List<Player>) = strategy.discuss(knowledge.toList())
    override fun selectTarget(context: SelectionContext) = strategy.selectTarget(context, knowledge.toList())
}
