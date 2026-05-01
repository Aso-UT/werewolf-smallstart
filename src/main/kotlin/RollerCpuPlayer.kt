package org.example

class RollerCpuPlayer(role: Role, override val name: String) : Player(role) {
    private var selectCount = 0

    override fun selectTarget(context: SelectionContext): Player {
        val candidates = context.candidates()
        return candidates[selectCount++ % candidates.size]
    }

    override fun discuss(players: List<Player>): Statement = Statement.Plain("")
    override fun onReceive(event: GameEvent) { /* does not use received events */ }
}
