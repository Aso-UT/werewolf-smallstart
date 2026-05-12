package org.example

open class NothingPlayer(role: Role, override val name: String) : Player(role) {
    override fun buildStatement(context: DiscussionContext): Statement = error("buildStatement not expected")
    override fun onReceive(event: GameEvent) { error("onReceive not expected") }
    override fun choose(context: SelectionContext): Choice = error("choose not expected")
    override fun watchEpilogue(chronicles: List<Recallable>) { error("watchEpilogue not expected") }
}
