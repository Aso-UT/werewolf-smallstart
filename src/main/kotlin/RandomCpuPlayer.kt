package org.example

class RandomCpuPlayer(role: Role, override val name: String) : CpuPlayer(role) {
    override fun choose(context: SelectionContext): Choice = Choice(this, context, context.candidates().random(), "ランダム選択")
    override fun buildStatement(context: DiscussionContext): Statement = Statement.Plain("")
    override fun onReceive(event: GameEvent) { /* does not use received events */ }
}
