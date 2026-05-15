package werewolf.game

class Oracle(private val roles: Map<Player, Role>) {
    private fun roleOf(player: Player): Role =
        requireNotNull(roles[player]) { "Player not in Oracle: ${player.name}" }

    fun revealRole(player: Player) = GameEvent.RoleAssigned.send(roleOf(player), player)

    fun werewolves(alivePlayers: List<Player>): List<Player> = alivePlayers.filter { roleOf(it) == Role.WEREWOLF }

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
    fun isWinner(player: Player, winningSide: Side): Boolean = roleOf(player).isWinner(winningSide)
}
