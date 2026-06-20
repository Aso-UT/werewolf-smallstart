package werewolf.lodge

import werewolf.game.GameSetup
import werewolf.game.Oracle
import werewolf.game.Player
import werewolf.game.PlayerManager
import werewolf.game.Role

abstract class Lodge(private val humanConnection: HumanConnection) {
    abstract fun assignments(): List<Pair<Player, Role>>

    fun setup() = humanConnection.setup()
    fun teardown() = humanConnection.teardown()

    protected fun createPlayer(role: Role, name: String) = humanConnection.createPlayer(role, name)

    fun create(): GameSetup {
        val assignments = assignments()
        val oracle = Oracle(assignments.toMap())
        val playerManager = PlayerManager(assignments.map { it.first }, oracle)
        return GameSetup(playerManager, oracle)
    }
}
