package werewolf

import werewolf.game.*

open class NothingPlayer(role: Role, override val name: String) : Player(role) {
    override fun speak(context: DiscussionContext): Claim = error("speak not expected")
    override fun onReceive(event: GameEvent) { error("onReceive not expected") }
    override fun choose(context: SelectionContext): Choice = error("choose not expected")
    override fun watchEpilogue(chronicles: List<ChronicleView>) { error("watchEpilogue not expected") }
}
