package werewolf.lodge

import werewolf.game.Player
import werewolf.game.Role

abstract class CpuLodge(humanConnection: HumanConnection) : Lodge(humanConnection) {
    protected abstract fun createCpuPlayer(role: Role, name: String): Player

    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        val players = listOf(createPlayer(roles[0], "1")) +
            roles.drop(1).mapIndexed { i, role -> createCpuPlayer(role, "${i + 2}") }
        return players.zip(roles)
    }
}
