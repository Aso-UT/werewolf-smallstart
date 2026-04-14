package org.example

enum class Role(val displayName: String, val side: Side, val divineResult: DivineResult, val mediumResult: MediumResult) {
    WEREWOLF("人狼", Side.WEREWOLF, DivineResult.WEREWOLF, MediumResult.WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction {
            val candidates = players.filterNot { it === self }
            val target = io.prompt(self.name, "夜の行動", "襲撃先を選んでください", candidates)
            return NightAction.Attack(target)
        }
    },
    SEER("占い師", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction {
            val target = players.filterNot { it === self }.random()
            return NightAction.Divine(target)
        }
        override fun normalNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction {
            val candidates = players.filterNot { it === self }
            val target = io.prompt(self.name, "夜の行動", "占う対象を選んでください", candidates)
            return NightAction.Divine(target)
        }
    },
    MEDIUM("霊能者", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction = NightAction.MediumReveal
        override fun normalNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction = NightAction.MediumReveal
    },
    VILLAGER("村人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction = NightAction.None
    },
    HUNTER("狩人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction {
            val candidates = players.filterNot { it === self }
            val target = io.prompt(self.name, "夜の行動", "護衛する対象を選んでください", candidates)
            return NightAction.Guard(target)
        }
    };

    abstract fun firstNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction
    abstract fun normalNightAction(self: Player, players: List<Player>, io: PlayerIO): NightAction

    fun nightAction(self: Player, players: List<Player>, io: PlayerIO, isFirstNight: Boolean): NightAction =
        if (isFirstNight) firstNightAction(self, players, io) else normalNightAction(self, players, io)
}
