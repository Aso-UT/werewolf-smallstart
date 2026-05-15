package werewolf.cpu

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

class RollerCpuPlayer(role: Role, override val name: String) : CpuPlayer(role) {
    private var selectCount = 0

    override fun choose(context: SelectionContext): Choice {
        val candidates = context.candidates()
        return Choice(this, context, candidates[selectCount++ % candidates.size], "順番通りに選択")
    }

    override fun speak(context: DiscussionContext): Claim = Claim(this, context, Statement.Plain(""), "発言戦略なし")
    override fun onReceive(event: GameEvent) { /* does not use received events */ }
}
