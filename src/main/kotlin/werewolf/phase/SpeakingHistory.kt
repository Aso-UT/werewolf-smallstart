package werewolf.phase

import werewolf.game.Player

// 議論で誰がどの順に発言したかの記録
class SpeakingHistory {
    private val speakers = mutableListOf<Player>()

    val totalCount: Int get() = speakers.size

    val lastSpeaker: Player? get() = speakers.lastOrNull()

    fun countOf(player: Player): Int = speakers.count { it === player }

    fun record(speaker: Player) {
        speakers.add(speaker)
    }
}
