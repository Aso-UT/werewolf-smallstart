package werewolf.phase

import werewolf.game.AllPlayers
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.GameRecap
import werewolf.game.Oracle
import werewolf.game.PlayerManager

class Epilogue(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val signal: GameOverSignal
) {
    fun perform() {
        when (signal) {
            is GameOverSignal.Completed -> {
                GameEvent.GameOver.send(signal.winningSide, AllPlayers(playerManager))
                playerManager.allPlayers.forEach { GameEvent.GameResult.send(oracle.isWinner(it, signal.winningSide), it) }
            }
            is GameOverSignal.Aborted -> Unit
        }
        showRecap()
    }

    private fun showRecap() {
        val events = GameRecap(playerManager, signal).events()
        playerManager.allPlayers.forEach { it.watchEpilogue(events) }
    }
}
