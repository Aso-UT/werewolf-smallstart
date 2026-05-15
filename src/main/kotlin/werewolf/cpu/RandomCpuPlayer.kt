package werewolf.cpu

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

class RandomCpuPlayer(role: Role, override val name: String) : CpuPlayer(role) {
    override fun choose(context: SelectionContext): Choice = Choice(this, context, context.candidates().random(), "ランダム選択")
    override fun speak(context: DiscussionContext): Claim = Claim(this, context, Statement.Plain(""), "発言戦略なし")
    override fun onReceive(event: GameEvent) { /* does not use received events */ }
}
