package org.example

class PlayerManager(allPlayers: List<Player>) {
    companion object {
        private const val DISCUSSION_ROUNDS = 3
    }

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

    private fun runNightActions(nightNumber: Int) {
        val decisions = _alivePlayers.map { it to it.role.buildNightAction(it, _alivePlayers, nightNumber == 1) }

        val attacks = decisions.map { it.second }.filterIsInstance<NightAction.Attack>()
        val guards = decisions.map { it.second }.filterIsInstance<NightAction.Guard>()
        attackIfNotGuarded(attacks, guards)

        revealNightSecrets(decisions)
    }

    private fun revealNightSecrets(decisions: List<Pair<Player, NightAction>>) {
        decisions.forEach { (player, decision) ->
            when (decision) {
                is NightAction.None -> Unit
                is NightAction.Attack -> Unit
                is NightAction.Guard -> Unit
                is NightAction.Divine -> player.receive(GameEvent.Divined(decision.target, decision.target.role.divineResult))
                is NightAction.MediumReveal -> _executedPlayers.lastOrNull()?.let { target ->
                    player.receive(GameEvent.MediumRevealed(target, target.role.mediumResult))
                }
            }
        }
    }

    private fun attackIfNotGuarded(attacks: List<NightAction.Attack>, guards: List<NightAction.Guard>) {
        val target = MajorityVoteResolver.resolve(attacks.map { it.target }) ?: return
        if (guards.none { it.target === target }) attack(target)
    }

    fun runTurn(nightNumber: Int) {
        runNightActions(nightNumber)
        runDiscussion()
        runVoting()
    }

    private fun runDiscussion() {
        repeat(DISCUSSION_ROUNDS) { index ->
            val statements = _alivePlayers.map { it.name to it.discuss(_alivePlayers) }
            _alivePlayers.forEach { it.receive(GameEvent.DiscussionRound(index + 1, statements)) }
        }
    }

    private fun runVoting() {
        val votes = _alivePlayers.map { it.selectTarget(SelectionContext.Vote(it, _alivePlayers)) }
        val mostVoted = MajorityVoteResolver.resolve(votes) ?: return
        execute(mostVoted)
    }

    fun endGame(signal: GameOverSignal) {
        _allPlayers.forEach { it.receive(GameEvent.GameOver(signal.winningSide)) }
    }

    private fun execute(mostVoted: Player) {
        _allPlayers.forEach { it.receive(GameEvent.PlayerExecuted(executed = mostVoted)) }
        _executedPlayers.add(mostVoted)
        die(mostVoted)
    }

    private fun attack(target: Player) {
        _allPlayers.forEach { it.receive(GameEvent.PlayerAttacked(attacked = target)) }
        _attackedPlayers.add(target)
        die(target)
    }
}