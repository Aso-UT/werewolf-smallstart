package org.example

class PlayerManager(setup: GameSetup) {

    private val oracle = setup.oracle
    private val _allPlayers: List<Player> = setup.players
    private val _alivePlayers: MutableList<Player> = _allPlayers.toMutableList()
    private val _executedPlayers: MutableList<Player> = mutableListOf()
    private val _attackedPlayers: MutableList<Player> = mutableListOf()
    val players: List<Player> get() = _alivePlayers

    private val allPlayers get() = AllPlayers(_allPlayers)

    private fun checkWinner() {
        GameOverSignal.throwIfGameOver(oracle.aliveCounts(_alivePlayers))
    }

    private fun die(player: Player) {
        _alivePlayers.remove(player)
        checkWinner()
    }

    fun startGame() {
        oracle.initiatePlayers()
    }

    private fun runNightActions(nightNumber: Int): Player? {
        val decisions = _alivePlayers.map { it to oracle.buildNightAction(it, _alivePlayers, nightNumber == 1) }

        val attacks = decisions.map { it.second }.filterIsInstance<NightAction.Attack>()
        val guards = decisions.map { it.second }.filterIsInstance<NightAction.Guard>()
        val killed = attackIfNotGuarded(attacks, guards)

        revealNightSecrets(decisions)
        return killed
    }

    private fun revealNightSecrets(decisions: List<Pair<Player, NightAction>>) {
        decisions.forEach { (player, decision) ->
            when (decision) {
                is NightAction.None -> Unit
                is NightAction.Attack -> Unit
                is NightAction.Guard -> Unit
                is NightAction.FirstNightDivine -> oracle.firstNightDivine(player, _alivePlayers)
                is NightAction.Divine -> GameEvent.Divined.send(decision.target, player)
                is NightAction.MediumReveal -> _executedPlayers.lastOrNull()?.let { target ->
                    GameEvent.MediumRevealed.send(target, player)
                }
            }
        }
    }

    private fun attackIfNotGuarded(attacks: List<NightAction.Attack>, guards: List<NightAction.Guard>): Player? {
        val target = MajorityVoteResolver.resolve(attacks.map { it.target }) ?: return null
        return if (guards.none { it.target === target }) attack(target) else null
    }

    fun runTurn(nightNumber: Int) {
        GameEvent.TimeChanged.send(TimeOfDay.Night(nightNumber), allPlayers)
        val killed = runNightActions(nightNumber)
        GameEvent.TimeChanged.send(TimeOfDay.Morning, allPlayers)
        GameEvent.MorningReport.send(killed, allPlayers)
        runDiscussion()
        runVoting()
    }

    private fun runDiscussion() {
        RandomOrderDiscussion(_alivePlayers, _allPlayers).conduct()
    }

    private fun runVoting() {
        val votes = _alivePlayers.map { it.selectTarget(SelectionContext.Vote(it, _alivePlayers)) }
        val mostVoted = MajorityVoteResolver.resolveNonEmpty(votes)
        execute(mostVoted)
    }

    fun endGame(signal: GameOverSignal) {
        GameEvent.GameOver.send(signal.winningSide, allPlayers)
        _allPlayers.forEach { GameEvent.GameResult.send(oracle.isWinner(it, signal.winningSide), it) }
    }

    private fun execute(mostVoted: Player) {
        GameEvent.PlayerExecuted.send(mostVoted, allPlayers)
        _executedPlayers.add(mostVoted)
        die(mostVoted)
    }

    private fun attack(target: Player): Player {
        GameEvent.PlayerAttacked.send(target, allPlayers)
        _attackedPlayers.add(target)
        die(target)
        return target
    }
}