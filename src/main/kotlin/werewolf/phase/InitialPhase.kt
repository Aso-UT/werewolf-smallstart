package werewolf.phase

import werewolf.game.GameEvent
import werewolf.game.Oracle
import werewolf.game.PlayerManager

class InitialPhase(private val playerManager: PlayerManager, private val oracle: Oracle) : Phase {
    override fun proceed(): Phase {
        playerManager.allPlayers.forEach { oracle.revealRole(it) }
        val wolves = oracle.werewolves(playerManager.players)
        wolves.forEach { self ->
            wolves.filterNot { it === self }.forEach { ally ->
                GameEvent.WerewolfAllyRevealed.send(ally, self)
            }
        }
        return NightPhase(playerManager, oracle, 1)
    }
}
