package werewolf

import werewolf.game.*
import werewolf.lodge.*

class TestLodge(private vararg val assignments: Pair<Player, Role>) : LocalLodge() {
    override fun assignments() = assignments.toList()
}
