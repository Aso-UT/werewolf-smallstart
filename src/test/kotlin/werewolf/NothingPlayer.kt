package werewolf

import werewolf.game.*
import werewolf.phase.*
import werewolf.cpu.*
import werewolf.ai.*
import werewolf.human.*
import werewolf.lodge.*

open class NothingPlayer(role: Role, override val name: String) : Player(role) {
    override fun speak(context: DiscussionContext): Claim = error("speak not expected")
    override fun onReceive(event: GameEvent) { error("onReceive not expected") }
    override fun choose(context: SelectionContext): Choice = error("choose not expected")
    override fun watchEpilogue(chronicles: List<Recallable>) { error("watchEpilogue not expected") }
}
