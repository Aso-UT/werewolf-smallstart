package org.example

sealed class DiscussionContext {
    abstract val round: Int
    abstract val day: Int
    abstract val players: List<Player>
    abstract val allPlayers: List<Player>
    abstract val title: String
    abstract val description: String
    abstract val availableTypes: Set<StatementType>

    data class Open(
        override val round: Int,
        override val day: Int,
        override val players: List<Player>,
        override val allPlayers: List<Player>,
    ) : DiscussionContext() {
        override val title = "議論"
        override val description = "全プレイヤーに向けた議論です。"
        override val availableTypes = StatementType.entries.toSet()
    }

    data class Conclave(
        override val round: Int,
        override val day: Int,
        override val players: List<Player>,
        override val allPlayers: List<Player>,
    ) : DiscussionContext() {
        override val title = "密談"
        override val description = "この会話は人狼にしか聞こえません。"
        override val availableTypes = setOf(StatementType.PLAIN)
    }
}
