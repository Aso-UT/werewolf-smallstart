package org.example

class HonestCpuPlayer(role: Role, override val name: String) : Player(role) {
    private val unspoken = mutableListOf<GameEvent>()
    private var selectCount = 0

    override fun onReceive(event: GameEvent) { unspoken.add(event) }

    override fun discuss(players: List<Player>): String {
        if (unspoken.isEmpty()) return ""
        return unspoken.removeFirst().body(this)
    }

    override fun selectTarget(context: SelectionContext): Player {
        val candidates = context.candidates()
        return candidates[selectCount++ % candidates.size]
    }
}
