package org.example

class PlayerManager(setup: GameSetup) {

    private val oracle = setup.oracle
    private val _allPlayers: List<Player> = setup.players
    private val _alivePlayers: MutableList<Player> = _allPlayers.toMutableList()
    private var _nightDeath: Player? = null
    val nightDeath: Player? get() = _nightDeath
    val players: List<Player> get() = _alivePlayers
    val allPlayers get() = AllPlayers(_allPlayers)

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

    fun runTurn(nightNumber: Int) {
        GameEvent.TimeChanged.send(TimeOfDay.Night(nightNumber), allPlayers)
        NightPhase(this, oracle, nightNumber).proceed()
        MorningPhase(this).proceed()
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

    fun kill(target: Player) {
        GameEvent.PlayerAttacked.send(target, allPlayers)
        _nightDeath = target
        die(target)
    }
}