package werewolf.lodge

import werewolf.game.Player
import werewolf.game.Role
import werewolf.ai.AiPersonalities
import werewolf.ai.AiPlayer
import werewolf.ai.Instruction
import werewolf.ai.poc.PocConsoleLanguageModel

object PocAiLodge : LocalLodge() {
    private val names = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi", "Ivan")

    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        val personalities = AiPersonalities.list.shuffled()
        return roles.mapIndexed { i, role ->
            AiPlayer(role, names[i], PocConsoleLanguageModel(), Instruction(names[i], personalities[i])) to role
        }
    }
}
