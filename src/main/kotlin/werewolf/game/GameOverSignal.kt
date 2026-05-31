package werewolf.game

sealed class GameOverSignal : Throwable() {
    class Completed(val winningSide: Side) : GameOverSignal()
    object Aborted : GameOverSignal()

    companion object {
        fun throwIfGameOver(aliveCounts: AliveCounts) {
            WinConditionChecker.winningSide(aliveCounts)
                ?.let { throw Completed(it) }
        }

        fun throwAborted(cause: Exception): Nothing {
            cause.printStackTrace()
            throw Aborted
        }

        fun throwManualAbort(): Nothing = throw Aborted
    }
}
