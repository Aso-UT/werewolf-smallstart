package org.example

class HonestCpuPlayer(role: Role, override val name: String) : CpuPlayer(role) {
    private val unspoken = mutableListOf<GameEvent>()
    private var selectCount = 0

    override fun onReceive(event: GameEvent) {
        if (!event.isPublicKnowledge()) unspoken.add(event)
    }

    override fun buildStatement(context: DiscussionContext): Statement {
        if (unspoken.isEmpty()) return Statement.Plain("")
        return Statement.Plain(unspoken.removeFirst().body())
    }

    override fun choose(context: SelectionContext): Choice {
        val candidates = context.candidates()
        return Choice(this, context, candidates[selectCount++ % candidates.size], "順番通りに選択")
    }
}
