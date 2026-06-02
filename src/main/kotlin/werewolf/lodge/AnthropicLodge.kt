package werewolf.lodge

import werewolf.ai.AiPersonalities
import werewolf.ai.AiPlayer
import werewolf.ai.Instruction
import werewolf.ai.anthropic.AnthropicLanguageModel
import werewolf.game.Player
import werewolf.game.Role
import werewolf.human.ConsolePlayerIO
import werewolf.human.HumanPlayer

object AnthropicLodge : Lodge() {
    private val aiNames = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi")

    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        val human = HumanPlayer(roles[0], "Ivan", ConsolePlayerIO()) to roles[0]
        val personalities = AiPersonalities.list.shuffled()
        val aiPlayers = roles.drop(1).mapIndexed { i, role -> AiPlayer(role, aiNames[i], AnthropicLanguageModel(), Instruction(aiNames[i], personalities[i])) to role }
        return listOf(human) + aiPlayers
    }
}
