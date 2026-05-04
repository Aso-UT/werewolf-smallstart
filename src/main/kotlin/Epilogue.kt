package org.example

class Epilogue(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val signal: GameOverSignal
) {
    fun perform() {
        GameEvent.GameOver.send(signal.winningSide, AllPlayers(playerManager))
        playerManager.allPlayers.forEach { GameEvent.GameResult.send(oracle.isWinner(it, signal.winningSide), it) }
        showRecap()
    }

    private fun showRecap() {
        val events = GameRecap(playerManager, signal).events()
        playerManager.allPlayers.forEach { it.watchEpilogue(events) }
    }
}
