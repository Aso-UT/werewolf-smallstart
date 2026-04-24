package org.example

abstract class Player(private val role: Role) : Notifiable {
    abstract val name: String
    abstract fun selectTarget(context: SelectionContext): Player
    abstract override fun receive(event: GameEvent)
    abstract fun discuss(players: List<Player>): String

    fun buildNightAction(players: List<Player>, isFirstNight: Boolean): NightAction =
        role.buildNightAction(this, players, isFirstNight)
}