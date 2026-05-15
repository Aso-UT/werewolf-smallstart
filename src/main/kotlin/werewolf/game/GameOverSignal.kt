package werewolf.game

class GameOverSignal private constructor(val winningSide: Side) : Throwable() {
    companion object {
        fun throwIfGameOver(aliveCounts: AliveCounts) {
            WinConditionChecker.winningSide(aliveCounts)
                ?.let { throw GameOverSignal(it) }
        }
    }
}
