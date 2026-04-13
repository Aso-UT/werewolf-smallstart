package org.example

interface Player {
    val name: String
    val role: Role
    fun notifyRole()
    fun nightAction(players: List<Player>, nightNumber: Int): NightAction
    fun vote(players: List<Player>): Player
    fun onPlayerExecuted(player: Player)
    fun onPlayerAttacked(player: Player)
    fun onDivineResult(target: Player, result: DivineResult)
    fun onMediumReveal(target: Player, result: MediumResult)
    fun onGameOver(winnerSide: Side)
}