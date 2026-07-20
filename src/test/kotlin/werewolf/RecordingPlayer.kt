package werewolf

import werewolf.game.GameEvent
import werewolf.game.Role

open class RecordingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
    val received = mutableListOf<GameEvent>()
    override fun onReceive(event: GameEvent) { received.add(event) }
}
