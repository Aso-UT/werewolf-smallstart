package werewolf.lodge

import werewolf.ai.AiPersonalities
import werewolf.ai.AiPlayer
import werewolf.ai.Instruction
import werewolf.ai.poc.PocConsoleLanguageModel
import werewolf.game.Player
import werewolf.game.Role

class PocAiLodge(humanConnection: HumanConnection) : Lodge(humanConnection) {
    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        val personalities = AiPersonalities.list.shuffled()
        val aiPlayers = roles.drop(1).mapIndexed { i, role ->
            AiPlayer(role, AI_NAMES[i], PocConsoleLanguageModel(), Instruction(AI_NAMES[i], personalities[i])) to role
        }
        return listOf(createPlayer(roles[0], "Ivan") to roles[0]) + aiPlayers
    }

    companion object {
        private val AI_NAMES = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi")
    }
}
