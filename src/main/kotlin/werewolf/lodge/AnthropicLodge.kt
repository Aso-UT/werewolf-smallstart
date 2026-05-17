package werewolf.lodge

import werewolf.ai.AiPlayer
import werewolf.ai.AnthropicLanguageModel
import werewolf.game.Player
import werewolf.game.Role
import werewolf.human.ConsolePlayerIO
import werewolf.human.HumanPlayer

object AnthropicLodge : Lodge() {
    private val aiNames = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi")

    override fun assignments(): List<Pair<Player, Role>> {
        val languageModel = AnthropicLanguageModel()
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        val human = HumanPlayer(roles[0], "Ivan", ConsolePlayerIO()) to roles[0]
        val aiPlayers = roles.drop(1).mapIndexed { i, role -> AiPlayer(role, aiNames[i], languageModel) to role }
        return listOf(human) + aiPlayers
    }
}
