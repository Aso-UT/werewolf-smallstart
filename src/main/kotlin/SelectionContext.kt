package org.example

interface CandidateFilter {
    fun candidates(self: Player, players: List<Player>): List<Player> = players.filterNot { it === self }
}

enum class SelectionContext(val title: String, val description: String) : CandidateFilter {
    ATTACK("夜の行動", "襲撃先を選んでください"),
    DIVINE("夜の行動", "占う対象を選んでください"),
    GUARD("夜の行動", "護衛する対象を選んでください"),
    VOTE("投票", "投票先を選んでください"),
}

object FirstDivineFilter : CandidateFilter {
    override fun candidates(self: Player, players: List<Player>): List<Player> =
        super.candidates(self, players).filter { it.role.divineResult == DivineResult.NOT_WEREWOLF }
}