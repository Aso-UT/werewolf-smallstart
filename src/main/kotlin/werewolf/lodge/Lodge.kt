package werewolf.lodge

import werewolf.game.GameSetup
import werewolf.game.Oracle
import werewolf.game.Player
import werewolf.game.PlayerManager
import werewolf.game.Role

abstract class Lodge {
    abstract fun assignments(): List<Pair<Player, Role>>
    abstract fun setup()
    abstract fun teardown()

    fun create(): GameSetup {
        val assignments = assignments()
        val oracle = Oracle(assignments.toMap())
        val playerManager = PlayerManager(assignments.map { it.first }, oracle)
        return GameSetup(playerManager, oracle)
    }
}
