package org.example

class Oracle(private val roles: Map<Player, Role>) {
    fun initiatePlayers() = roles.forEach { (player, role) -> GameEvent.RoleAssigned.send(role, player) }

    // Temporary: delegating to Role is Player's responsibility. Will be removed in #60
    // when Player becomes an abstract class with buildNightAction as a final method.
    fun buildNightAction(player: Player, alivePlayers: List<Player>, isFirstNight: Boolean): NightAction =
        roles[player]!!.buildNightAction(player, alivePlayers, isFirstNight)

    fun aliveCounts(alivePlayers: List<Player>): AliveCounts =
        AliveCounts(Side.entries.associateWith { side -> alivePlayers.count { roles[it]!!.side == side } })
    fun isWinner(player: Player, winningSide: Side): Boolean = roles[player]!!.side == winningSide
}
