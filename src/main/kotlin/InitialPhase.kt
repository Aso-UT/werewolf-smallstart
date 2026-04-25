package org.example

class InitialPhase(private val playerManager: PlayerManager, private val oracle: Oracle) : Phase {
    override fun proceed(): Phase {
        oracle.initiatePlayers()
        return NightPhase(playerManager, oracle, 1)
    }
}
