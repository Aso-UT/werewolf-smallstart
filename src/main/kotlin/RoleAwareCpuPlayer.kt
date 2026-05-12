package org.example

class RoleAwareCpuPlayer(val myRole: Role, override val name: String) : CpuPlayer(myRole) {
    private val _knowledge = mutableListOf<GameEvent>()
    val knowledge: List<GameEvent> get() = _knowledge.toList()

    private val strategy = setOf(
        SeerCpuStrategy(this), MediumCpuStrategy(this), WerewolfCpuStrategy(this),
        HunterCpuStrategy(this), VillagerCpuStrategy(this), MadmanCpuStrategy(this)
    ).single { it.appliesTo() }

    override fun onReceive(event: GameEvent) { _knowledge.add(event) }
    override fun speak(context: DiscussionContext) = Claim(this, context, strategy.buildStatement(context), "役職戦略に基づく発言")
    override fun choose(context: SelectionContext) = Choice(this, context, strategy.selectTarget(context), "戦略による選択")
}
