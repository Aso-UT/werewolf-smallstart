package org.example

object PocAiLodge : Lodge() {
    private val names = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi", "Ivan")

    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        return roles.mapIndexed { i, role -> PocAiPlayer(role, names[i]) to role }
    }
}
