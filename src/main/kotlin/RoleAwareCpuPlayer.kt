package org.example

class RoleAwareCpuPlayer(val myRole: Role, override val name: String) : CpuPlayer(myRole) {
    private val _knowledge = mutableListOf<GameEvent>()
    val knowledge: List<GameEvent> get() = _knowledge.toList()

    private val strategy = setOf(
        SeerCpuStrategy(this), MediumCpuStrategy(this), WerewolfCpuStrategy(this),
        HunterCpuStrategy(this), VillagerCpuStrategy(this), MadmanCpuStrategy(this)
    ).single { it.appliesTo() }

    override fun onReceive(event: GameEvent) { _knowledge.add(event) }
    override fun buildStatement(context: DiscussionContext) = strategy.buildStatement(context)
    override fun choose(context: SelectionContext) = Choice(this, context, strategy.selectTarget(context), "戦略による選択")
}
