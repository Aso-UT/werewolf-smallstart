package org.example

open class NothingPlayer(role: Role, override val name: String) : Player(role) {
    override fun buildStatement(context: DiscussionContext): Statement = error("buildStatement not expected")
    override fun onReceive(event: GameEvent) { error("onReceive not expected") }
    override fun selectTarget(context: SelectionContext): Player = error("selectTarget not expected")
    override fun watchEpilogue(memories: List<Recallable>) { error("watchEpilogue not expected") }
}
