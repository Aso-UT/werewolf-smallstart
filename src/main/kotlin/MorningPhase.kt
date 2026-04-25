package org.example

class MorningPhase(private val playerManager: PlayerManager) : Phase {
    override fun proceed() {
        GameEvent.TimeChanged.send(TimeOfDay.Morning, playerManager.allPlayers)
        GameEvent.MorningReport.send(playerManager.nightDeath, playerManager.allPlayers)
        playerManager.bury()
    }
}