package org.example

object PocAiLodge : Lodge() {
    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        return roles.mapIndexed { i, role -> PocAiPlayer(role, "${i + 1}") to role }
    }
}
