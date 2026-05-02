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

    fun execute(mostVoted: Player) {
        GameEvent.PlayerExecuted.send(mostVoted, allPlayers)
        die(mostVoted)
    }

    fun kill(target: Player) {
        GameEvent.PlayerAttacked.send(target, allPlayers)
        _nightDeath = target
        die(target)
    }
}
