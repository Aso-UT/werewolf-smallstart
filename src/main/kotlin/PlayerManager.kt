package org.example

class PlayerManager(setup: GameSetup) {

    private val oracle = setup.oracle
    private val _allPlayers: List<Player> = setup.players
    private val _alivePlayers: MutableList<Player> = _allPlayers.toMutableList()
    private var _nightDeath: Player? = null
    val players: List<Player> get() = _alivePlayers

    private val allPlayers get() = AllPlayers(_allPlayers)

    private fun checkWinner() {
        GameOverSignal.throwIfGameOver(oracle.aliveCounts(_alivePlayers))
    }

    private fun die(player: Player) {
        _alivePlayers.remove(player)
        checkWinner()
    }

    fun bury() {
        _nightDeath = null
    }

    fun startGame() {
        oracle.initiatePlayers()
    }

    private fun runNightActions(nightNumber: Int) {
        val decisions = _alivePlayers.map { it to it.buildNightAction(_alivePlayers, nightNumber == 1) }

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
                is NightAction.FirstNightDivine -> oracle.firstNightDivine(player, _alivePlayers)
                is NightAction.Divine -> oracle.divine(player, decision.target)
                is NightAction.MediumReveal -> oracle.mediumReveal(player, decision.target)
            }
        }
    }

    private fun attackIfNotGuarded(attacks: List<NightAction.Attack>, guards: List<NightAction.Guard>) {
        val target = MajorityVoteResolver.resolve(attacks.map { it.target }) ?: return
        if (guards.none { it.target === target }) attack(target)
    }

    fun runTurn(nightNumber: Int) {
        GameEvent.TimeChanged.send(TimeOfDay.Night(nightNumber), allPlayers)
        runNightActions(nightNumber)
        GameEvent.TimeChanged.send(TimeOfDay.Morning, allPlayers)
        GameEvent.MorningReport.send(_nightDeath, allPlayers)
        bury()
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
        die(mostVoted)
    }

    private fun attack(target: Player) {
        GameEvent.PlayerAttacked.send(target, allPlayers)
        _nightDeath = target
        die(target)
    }
}