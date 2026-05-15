package werewolf.game

enum class Side(val displayName: String) {
    CITIZEN("市民") {
        override fun hasWon(aliveCounts: AliveCounts): Boolean =
            aliveCounts[WEREWOLF] == 0
    },
    WEREWOLF("人狼") {
        override fun hasWon(aliveCounts: AliveCounts): Boolean =
            aliveCounts[WEREWOLF] >= aliveCounts[CITIZEN]
    };

    abstract fun hasWon(aliveCounts: AliveCounts): Boolean
}
