package org.example

class RoleAwareCpuPlayer(val myRole: Role, override val name: String) : CpuPlayer(myRole) {
    private val _knowledge = mutableListOf<GameEvent>()
    val knowledge: List<GameEvent> get() = _knowledge.toList()

    private val strategy = setOf(
        SeerCpuStrategy(this), MediumCpuStrategy(this), WerewolfCpuStrategy(this),
        HunterCpuStrategy(this), VillagerCpuStrategy(this)
    ).single { it.appliesTo() }

    override fun onReceive(event: GameEvent) { _knowledge.add(event) }
    override fun discuss(players: List<Player>) = strategy.discuss()
    override fun selectTarget(context: SelectionContext) = strategy.selectTarget(context)
}
