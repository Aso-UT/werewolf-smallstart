package org.example

class Oracle(private val roles: Map<Player, Role>) {
    private fun roleOf(player: Player): Role =
        requireNotNull(roles[player]) { "Player not in Oracle: ${player.name}" }

    fun initiatePlayers() = roles.forEach { (player, role) -> GameEvent.RoleAssigned.send(role, player) }

    // Temporary: delegating to Role is Player's responsibility. Will be removed in #60
    // when Player becomes an abstract class with buildNightAction as a final method.
    fun buildNightAction(player: Player, alivePlayers: List<Player>, isFirstNight: Boolean): NightAction =
        roleOf(player).buildNightAction(player, alivePlayers, isFirstNight)

    fun divine(seer: Player, target: Player) =
        GameEvent.Divined.send(target, roleOf(target).divineResult, seer)

    fun mediumReveal(medium: Player, target: Player) =
        GameEvent.MediumRevealed.send(target, roleOf(target).mediumResult, medium)

    fun firstNightDivine(seer: Player, alivePlayers: List<Player>) {
        val target = alivePlayers.filterNot { it === seer }
            .filter { roleOf(it).divineResult == DivineResult.NOT_WEREWOLF }
            .random()
        divine(seer, target)
    }

    fun aliveCounts(alivePlayers: List<Player>): AliveCounts =
        AliveCounts(Side.entries.associateWith { side -> alivePlayers.count { roleOf(it).side == side } })
    fun isWinner(player: Player, winningSide: Side): Boolean = roleOf(player).side == winningSide
}
