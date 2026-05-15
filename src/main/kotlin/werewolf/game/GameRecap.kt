package werewolf.game

class GameRecap(private val playerManager: PlayerManager, private val signal: GameOverSignal) {
    fun events(): List<Recallable> =
        playerManager.allPlayers
            .flatMap { it.reveal(signal) }
            .distinct()
            .sortedBy { it.sequenceId }
}
