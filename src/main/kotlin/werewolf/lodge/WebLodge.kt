package werewolf.lodge

import werewolf.ai.AiPersonalities
import werewolf.ai.AiPlayer
import werewolf.ai.Instruction
import werewolf.ai.anthropic.AnthropicLanguageModel
import werewolf.game.Player
import werewolf.game.Role
import werewolf.web.WebPlayer

class WebLodge : Lodge() {
    private val aiNames = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi")

    lateinit var webPlayer: WebPlayer
        private set

    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        webPlayer = WebPlayer(roles[0], "Ivan")
        val personalities = AiPersonalities.list.shuffled()
        val aiPlayers: List<Pair<Player, Role>> = roles.drop(1).mapIndexed { i, role ->
            AiPlayer(role, aiNames[i], AnthropicLanguageModel(HAIKU_MODEL), Instruction(aiNames[i], personalities[i])) to role
        }
        return listOf(webPlayer to roles[0]) + aiPlayers
    }

    companion object {
        private const val HAIKU_MODEL = "claude-haiku-4-5-20251001"
    }
}
