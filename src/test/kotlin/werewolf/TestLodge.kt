package werewolf

import werewolf.game.*
import werewolf.phase.*
import werewolf.cpu.*
import werewolf.ai.*
import werewolf.human.*
import werewolf.lodge.*

class TestLodge(private vararg val assignments: Pair<Player, Role>) : Lodge() {
    override fun assignments() = assignments.toList()
}
