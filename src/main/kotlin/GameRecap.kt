package org.example

class GameRecap(private val playerManager: PlayerManager, private val signal: GameOverSignal) {
    fun events(): List<GameEvent> =
        playerManager.allPlayers
            .flatMap { it.revealKnowledge(signal) }
            .distinct()
            .sortedBy { it.sequenceId }
}
