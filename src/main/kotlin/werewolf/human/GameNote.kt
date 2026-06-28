package werewolf.human

import werewolf.game.GameEvent
import werewolf.view.PlayerStatus
import werewolf.view.SurvivalView

class GameNote {
    private val playerStatuses = mutableMapOf<String, PlayerStatus>()

    fun post(event: GameEvent) {
        when (event) {
            is GameEvent.PlayersAnnounced -> event.players.forEach { playerStatuses[it.name] = PlayerStatus.ALIVE }
            is GameEvent.PlayerExecuted -> playerStatuses[event.executed.name] = PlayerStatus.EXECUTED
            is GameEvent.PlayerAttacked -> playerStatuses[event.attacked.name] = PlayerStatus.ATTACKED
            else -> Unit
        }
    }

    fun summary(): SurvivalView = SurvivalView(playerStatuses.toMap())
}
