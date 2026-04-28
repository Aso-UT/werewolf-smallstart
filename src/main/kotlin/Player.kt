package org.example

abstract class Player(private val role: Role) : Notifiable {
    abstract val name: String
    override val recipientName: String get() = name
    private val _knowledge: MutableList<GameEvent> = mutableListOf()

    final override fun receive(event: GameEvent) {
        _knowledge.add(event)
        onReceive(event)
    }

    protected abstract fun onReceive(event: GameEvent)
    abstract fun selectTarget(context: SelectionContext): Player
    abstract fun discuss(players: List<Player>): Statement

    fun revealKnowledge(signal: GameOverSignal): List<GameEvent> = _knowledge.toList()

    fun buildNightAction(players: List<Player>, isFirstNight: Boolean): NightAction =
        role.buildNightAction(this, players, isFirstNight, _knowledge.toList())
}