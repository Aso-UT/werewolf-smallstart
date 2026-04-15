package org.example

interface Player {
    val name: String
    val role: Role
    fun notifyRole()
    fun nightAction(players: List<Player>, isFirstNight: Boolean): NightAction
    fun vote(players: List<Player>): Player
    fun onPlayerExecuted(player: Player)
    fun onPlayerAttacked(player: Player)
    fun onDivineResult(target: Player, result: DivineResult)
    fun onMediumReveal(target: Player, result: MediumResult)
    fun discuss(players: List<Player>): String
    fun onDiscussionRound(round: Int, statements: List<Pair<String, String>>)
    fun onGameOver(winnerSide: Side)
}