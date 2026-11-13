package werewolf.phase

import werewolf.game.Player
import werewolf.game.Statement

class SpeakingHistory {
    private val statements = mutableListOf<Statement>()

    val totalCount: Int get() = statements.size

    val lastStatement: Statement? get() = statements.lastOrNull()

    fun countOf(player: Player): Int = statements.count { it.claimant === player }

    fun nextRoundOf(player: Player): Int = countOf(player) + 1

    fun isLastSpeaker(player: Player): Boolean = lastStatement?.claimant === player

    fun record(statement: Statement) {
        statements.add(statement)
    }
}
