package org.example

abstract class Player(private val role: Role) : Notifiable {
    abstract val name: String
    private val _receivedEvents: MutableList<GameEvent> = mutableListOf()
    val receivedEvents: List<GameEvent> get() = _receivedEvents

    final override fun receive(event: GameEvent) {
        _receivedEvents.add(event)
        onReceive(event)
    }

    protected abstract fun onReceive(event: GameEvent)
    abstract fun selectTarget(context: SelectionContext): Player
    abstract fun discuss(players: List<Player>): String

    fun buildNightAction(players: List<Player>, isFirstNight: Boolean): NightAction =
        role.buildNightAction(this, players, isFirstNight)
}