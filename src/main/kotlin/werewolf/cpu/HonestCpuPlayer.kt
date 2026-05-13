package werewolf.cpu

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

class HonestCpuPlayer(role: Role, override val name: String) : CpuPlayer(role) {
    private val unspoken = mutableListOf<GameEvent>()
    private var selectCount = 0

    override fun onReceive(event: GameEvent) {
        if (!event.isPublicKnowledge()) unspoken.add(event)
    }

    override fun speak(context: DiscussionContext): Claim {
        val statement = if (unspoken.isEmpty()) Statement.Plain("") else Statement.Plain(unspoken.removeFirst().body())
        return Claim(this, context, statement, "受け取った非公開情報を順番に開示")
    }

    override fun choose(context: SelectionContext): Choice {
        val candidates = context.candidates()
        return Choice(this, context, candidates[selectCount++ % candidates.size], "順番通りに選択")
    }
}
