package org.example

enum class Role(val displayName: String, val side: Side, val divineResult: DivineResult, val mediumResult: MediumResult) {
    WEREWOLF("人狼", Side.WEREWOLF, DivineResult.WEREWOLF, MediumResult.WEREWOLF) {
        override fun nightAction(self: Player, players: List<Player>, io: PlayerIO, nightNumber: Int): NightAction {
            if (nightNumber == 1) return NightAction.None
            val candidates = players.filterNot { it === self }
            val target = io.prompt(self.name, "夜の行動", "襲撃先を選んでください", candidates)
            return NightAction.Attack(target)
        }
    },
    SEER("占い師", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun nightAction(self: Player, players: List<Player>, io: PlayerIO, nightNumber: Int): NightAction {
            val candidates = players.filterNot { it === self }
            val target = if (nightNumber == 1)
                candidates.random()
            else
                io.prompt(self.name, "夜の行動", "占う対象を選んでください", candidates)
            return NightAction.Divine(target)
        }
    },
    MEDIUM("霊能者", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun nightAction(self: Player, players: List<Player>, io: PlayerIO, nightNumber: Int): NightAction =
            NightAction.MediumReveal
    },
    VILLAGER("村人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun nightAction(self: Player, players: List<Player>, io: PlayerIO, nightNumber: Int): NightAction =
            NightAction.None
    };

    abstract fun nightAction(self: Player, players: List<Player>, io: PlayerIO, nightNumber: Int): NightAction
}