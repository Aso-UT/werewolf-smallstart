package werewolf.game

class GameRecap(private val playerManager: PlayerManager, private val signal: GameOverSignal) {
    fun events(): List<Recallable> =
        playerManager.allPlayers
            .flatMap { it.reveal(signal) }
            .distinct()
            .sortedBy { it.sequenceId }

    fun chronicles(): List<ChronicleView> =
        events()
            .filterNot { isRedundantWithClaim(it) }
            .map { it.toChronicleView() }

    private fun isRedundantWithClaim(recallable: Recallable): Boolean =
        recallable is GameEvent.StatementMade || recallable is GameEvent.WerewolfStatementMade
}
