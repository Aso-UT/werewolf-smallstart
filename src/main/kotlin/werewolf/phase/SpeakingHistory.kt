package werewolf.phase

import werewolf.game.Player
import werewolf.game.Statement

// 議論でこれまでに出た発言。同時発言は、全員の発言をひとまとまりとして記録する
class SpeakingHistory {
    private val turns = mutableListOf<List<Statement>>()

    val totalCount: Int get() = turns.sumOf { it.size }

    val lastStatements: List<Statement> get() = turns.lastOrNull().orEmpty()

    fun countOf(player: Player): Int = turns.sumOf { turn -> turn.count { it.claimant === player } }

    fun nextRoundOf(player: Player): Int = countOf(player) + 1

    // 同時発言に加わった人は全員、直前の発言者とみなす
    fun isLastSpeaker(player: Player): Boolean = lastStatements.any { it.claimant === player }

    fun record(statements: List<Statement>) {
        turns.add(statements)
    }
}
