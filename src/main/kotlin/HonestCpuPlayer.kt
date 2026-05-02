package org.example

class HonestCpuPlayer(role: Role, override val name: String) : CpuPlayer(role) {
    private val unspoken = mutableListOf<GameEvent>()
    private var selectCount = 0

    override fun onReceive(event: GameEvent) {
        if (!event.isPublicKnowledge()) unspoken.add(event)
    }

    override fun discuss(players: List<Player>): Statement {
        if (unspoken.isEmpty()) return Statement.Plain("")
        return Statement.Plain(unspoken.removeFirst().body())
    }

    override fun selectTarget(context: SelectionContext): Player {
        val candidates = context.candidates()
        return candidates[selectCount++ % candidates.size]
    }
}
