package org.example

class EndPhase(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val signal: GameOverSignal
) : Phase {
    override fun proceed(): Phase {
        GameEvent.GameOver.send(signal.winningSide, playerManager.allPlayers)
        playerManager.allPlayers.forEach { GameEvent.GameResult.send(oracle.isWinner(it, signal.winningSide), it) }
        showRecap()
        return this
    }

    private fun showRecap() {
        println("=== ゲーム振り返り ===")
        GameRecap(playerManager, signal).events()
            .forEach { println("  ${it.recipientName}: [${it.title}] ${it.body()}") }
    }
}
