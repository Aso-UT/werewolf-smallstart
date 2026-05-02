package org.example

class MorningPhase(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val nightNumber: Int
) : Phase {
    override fun proceed(): Phase {
        GameEvent.TimeChanged.send(TimeOfDay.Morning, playerManager.allPlayers)
        GameEvent.MorningReport.send(playerManager.nightDeath, playerManager.allPlayers)
        playerManager.bury()
        return DayPhase(playerManager, oracle, nightNumber)
    }
}
