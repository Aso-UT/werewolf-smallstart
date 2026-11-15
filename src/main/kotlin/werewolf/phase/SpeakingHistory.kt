package werewolf.phase

import werewolf.game.Player
import werewolf.game.Statement

// 一つのターンでは、一人か複数人が同時に発言する
class SpeakingHistory {
    private val turns = mutableListOf<List<Statement>>()

    val totalCount: Int get() = turns.sumOf { it.size }

    val lastTurnStatements: List<Statement> get() = turns.lastOrNull().orEmpty()

    fun countOf(player: Player): Int = turns.sumOf { turn -> turn.count { it.claimant === player } }

    fun nextRoundOf(player: Player): Int = countOf(player) + 1

    fun isLastTurnSpeaker(player: Player): Boolean = lastTurnStatements.any { it.claimant === player }

    fun record(statements: List<Statement>) {
        turns.add(statements)
    }
}
