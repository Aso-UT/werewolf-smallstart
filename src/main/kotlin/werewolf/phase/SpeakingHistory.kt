package werewolf.phase

import werewolf.game.Player
import werewolf.game.Turn

class SpeakingHistory {
    private val turns = mutableListOf<Turn>()

    val totalCount: Int get() = turns.sumOf { it.count() }

    val lastTurn: Turn? get() = turns.lastOrNull()

    fun countOf(player: Player): Int = turns.sumOf { turn -> turn.count { it.claimant === player } }

    fun nextRoundOf(player: Player): Int = countOf(player) + 1

    fun isLastTurnSpeaker(player: Player): Boolean = lastTurn?.speakers.orEmpty().any { it === player }

    fun record(turn: Turn) {
        turns.add(turn)
    }
}
