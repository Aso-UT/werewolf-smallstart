package org.example

class RandomCpuPlayer(role: Role, override val name: String) : CpuPlayer(role) {
    override fun selectTarget(context: SelectionContext): Player = context.candidates().random()
    override fun discuss(players: List<Player>): Statement = Statement.Plain("")
    override fun onReceive(event: GameEvent) { /* does not use received events */ }
}
