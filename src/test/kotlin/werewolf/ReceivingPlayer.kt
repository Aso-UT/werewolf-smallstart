package werewolf

import werewolf.game.*
import werewolf.phase.*
import werewolf.cpu.*
import werewolf.ai.*
import werewolf.human.*
import werewolf.lodge.*

open class ReceivingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
    override fun onReceive(event: GameEvent) { /* accepts and ignores events broadcast to all players */ }
}
