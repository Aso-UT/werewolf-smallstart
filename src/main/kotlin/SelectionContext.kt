package org.example

sealed class SelectionContext(val title: String, val description: String) {
    abstract fun candidates(): List<Player>

    class Attack(private val self: Player, private val players: List<Player>)
        : SelectionContext("夜の行動", "襲撃先を選んでください") {
        override fun candidates() = players.filterNot { it === self || it.role == Role.WEREWOLF }
    }

    class Divine(private val self: Player, private val players: List<Player>)
        : SelectionContext("夜の行動", "占う対象を選んでください") {
        override fun candidates() = players.filterNot { it === self }
    }

    class Guard(private val self: Player, private val players: List<Player>)
        : SelectionContext("夜の行動", "護衛する対象を選んでください") {
        override fun candidates() = players.filterNot { it === self }
    }

    class Vote(private val self: Player, private val players: List<Player>)
        : SelectionContext("投票", "投票先を選んでください") {
        override fun candidates() = players.filterNot { it === self }
    }
}

object FirstDivineFilter {
    fun candidates(self: Player, players: List<Player>): List<Player> =
        players.filterNot { it === self }.filter { it.role.divineResult == DivineResult.NOT_WEREWOLF }
}