package org.example

enum class Role(val displayName: String, val side: Side, val divineResult: DivineResult, val mediumResult: MediumResult) {
    WEREWOLF("人狼", Side.WEREWOLF, DivineResult.WEREWOLF, MediumResult.WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction {
            val allies = knowledge
                .filterIsInstance<GameEvent.WerewolfAllyRevealed>()
                .map { it.ally }
            return NightAction.Attack(self.selectTarget(SelectionContext.Attack(self, players, allies)))
        }
    },
    SEER("占い師", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>): NightAction =
            NightAction.FirstNightDivine
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction =
            NightAction.Divine(self.selectTarget(SelectionContext.Divine(self, players)))
    },
    MEDIUM("霊能者", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>): NightAction = NightAction.MediumReveal
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.MediumReveal
    },
    VILLAGER("村人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.None
    },
    HUNTER("狩人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction =
            NightAction.Guard(self.selectTarget(SelectionContext.Guard(self, players)))
    };

    abstract fun firstNightAction(self: Player, players: List<Player>): NightAction
    abstract fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction

    fun buildNightAction(self: Player, players: List<Player>, isFirstNight: Boolean, knowledge: List<GameEvent>): NightAction =
        if (isFirstNight) firstNightAction(self, players) else normalNightAction(self, players, knowledge)
}
