package werewolf.game

enum class Role(val displayName: String, val side: Side, val divineResult: DivineResult, val mediumResult: MediumResult, val winningSide: Side = side) {
    WEREWOLF("人狼", Side.WEREWOLF, DivineResult.WEREWOLF, MediumResult.WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction {
            val allies = knowledge
                .filterIsInstance<GameEvent.WerewolfAllyRevealed>()
                .map { it.ally }
            return NightAction.Attack(self.selectTarget(SelectionContext.Attack(self, players, allies)))
        }
    },
    SEER("占い師", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction =
            NightAction.FirstNightDivine
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction =
            NightAction.Divine(self.selectTarget(SelectionContext.Divine(self, players)))
    },
    MEDIUM("霊能者", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction =
            normalNightAction(self, players, knowledge)
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction {
            val revealedTargets = knowledge.filterIsInstance<GameEvent.MediumRevealed>().map { it.target }
            val nextTarget = knowledge.filterIsInstance<GameEvent.PlayerExecuted>()
                .map { it.executed }
                .firstOrNull { executed -> revealedTargets.none { it === executed } }
            return if (nextTarget != null) NightAction.MediumReveal(nextTarget) else NightAction.None
        }
    },
    VILLAGER("村人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.None
    },
    HUNTER("狩人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction =
            NightAction.Guard(self.selectTarget(SelectionContext.Guard(self, players)))
    },
    MADMAN("狂人", Side.CITIZEN, DivineResult.NOT_WEREWOLF, MediumResult.NOT_WEREWOLF, Side.WEREWOLF) {
        override fun firstNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.None
        override fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction = NightAction.None
    };

    abstract fun firstNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction
    abstract fun normalNightAction(self: Player, players: List<Player>, knowledge: List<GameEvent>): NightAction

    fun buildNightAction(self: Player, players: List<Player>, isFirstNight: Boolean, knowledge: List<GameEvent>): NightAction =
        if (isFirstNight) firstNightAction(self, players, knowledge) else normalNightAction(self, players, knowledge)

    fun isWinner(winningSide: Side) = this.winningSide == winningSide
}
