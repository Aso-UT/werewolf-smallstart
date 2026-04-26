package org.example

class RandomCpuPlayer(role: Role, override val name: String) : Player(role) {
    override fun selectTarget(context: SelectionContext): Player = context.candidates().random()
    override fun discuss(players: List<Player>) = ""
    override fun onReceive(event: GameEvent) {}
}
