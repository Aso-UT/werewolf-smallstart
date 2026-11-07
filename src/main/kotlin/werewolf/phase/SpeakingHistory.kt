package werewolf.phase

import werewolf.game.Player

class SpeakingHistory {
    private val speakers = mutableListOf<Player>()

    val totalCount: Int get() = speakers.size

    fun countOf(player: Player): Int = speakers.count { it === player }

    fun nextRoundOf(player: Player): Int = countOf(player) + 1

    fun isLastSpeaker(player: Player): Boolean = speakers.lastOrNull() === player

    fun record(speaker: Player) {
        speakers.add(speaker)
    }
}
