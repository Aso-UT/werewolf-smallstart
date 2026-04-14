package org.example

class PlayerManager(allPlayers: List<Player>) {
    private val _allPlayers: List<Player> = allPlayers
    private val _alivePlayers: MutableList<Player> = _allPlayers.toMutableList()
    private val _executedPlayers: MutableList<Player> = mutableListOf()
    private val _attackedPlayers: MutableList<Player> = mutableListOf()
    val players: List<Player> get() = _alivePlayers

    private fun checkWinner() {
        GameOverSignal.throwIfGameOver(AliveCounts(_alivePlayers))
    }

    private fun die(player: Player) {
        _alivePlayers.remove(player)
        checkWinner()
    }

    fun startGame() {
        _allPlayers.forEach { it.notifyRole() }
    }

    fun runNightActions(nightNumber: Int) {
        val decisions = _alivePlayers.map { it to it.nightAction(_alivePlayers, nightNumber) }
        decisions.forEach { (player, decision) ->
            when (decision) {
                is NightAction.None -> Unit
                is NightAction.Attack -> attack(decision.target)
                is NightAction.Divine -> player.onDivineResult(decision.target, decision.target.role.divineResult)
                is NightAction.MediumReveal -> _executedPlayers.lastOrNull()?.let { target ->
                    player.onMediumReveal(target, target.role.mediumResult)
                }
                is NightAction.Guard -> Unit
            }
        }
    }

    fun runVoting() {
        val votes = _alivePlayers.map { it.vote(_alivePlayers) }
        val mostVoted = votes.groupBy { it }.maxBy { it.value.size }.key
        execute(mostVoted)
    }

    fun endGame(signal: GameOverSignal) {
        _allPlayers.forEach { it.onGameOver(signal.winningSide) }
    }

    private fun execute(mostVoted: Player) {
        _allPlayers.forEach { it.onPlayerExecuted(mostVoted) }
        _executedPlayers.add(mostVoted)
        die(mostVoted)
    }

    private fun attack(target: Player) {
        _allPlayers.forEach { it.onPlayerAttacked(target) }
        _attackedPlayers.add(target)
        die(target)
    }
}