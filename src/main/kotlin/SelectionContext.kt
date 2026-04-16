package org.example

enum class SelectionContext(val title: String, val description: String) {
    ATTACK("夜の行動", "襲撃先を選んでください"),
    DIVINE("夜の行動", "占う対象を選んでください"),
    FIRST_DIVINE("夜の行動", "占う対象を選んでください") {
        override fun candidates(self: Player, players: List<Player>): List<Player> =
            super.candidates(self, players).filter { it.role.divineResult == DivineResult.NOT_WEREWOLF }
    },
    GUARD("夜の行動", "護衛する対象を選んでください"),
    VOTE("投票", "投票先を選んでください");

    open fun candidates(self: Player, players: List<Player>): List<Player> = players.filterNot { it === self }
}
