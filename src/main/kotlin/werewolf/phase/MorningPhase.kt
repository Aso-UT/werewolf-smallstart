package werewolf.phase

import werewolf.game.AllPlayers
import werewolf.game.GameEvent
import werewolf.game.Oracle
import werewolf.game.PlayerManager
import werewolf.game.TimeOfDay

class MorningPhase(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val nightNumber: Int
) : Phase {
    override fun proceed(): Phase {
        GameEvent.TimeChanged.send(TimeOfDay.Morning, AllPlayers(playerManager))
        GameEvent.MorningReport.send(playerManager.nightDeath, AllPlayers(playerManager))
        playerManager.bury()
        return DayPhase(playerManager, oracle, nightNumber)
    }
}
