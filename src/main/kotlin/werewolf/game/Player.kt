package werewolf.game

abstract class Player(private val role: Role) : Notifiable {
    abstract val name: String
    override val recipientName: String get() = name
    // role and _memories are each player's private information and must not be visible to other players.
    // protected would allow subclasses to access any Player instance's data without casting,
    // breaking information asymmetry. Subclasses that need their own memories should manage a separate copy.
    private val _memories: MutableList<Recallable> = mutableListOf()

    final override fun receive(event: GameEvent) {
        _memories.add(event)
        onReceive(event)
    }

    protected abstract fun onReceive(event: GameEvent)
    fun selectTarget(context: SelectionContext): Player {
        val choice = choose(context)
        require(choice.chooser === this) { "${choice.chooser.name} is not the choosing player" }
        _memories.add(choice)
        return choice.selected
    }
    protected abstract fun choose(context: SelectionContext): Choice
    fun discuss(context: DiscussionContext): Statement {
        val claim = speak(context)
        require(claim.speaker === this) { "${claim.speaker.name} is not the speaking player" }
        _memories.add(claim)
        return claim.statement
    }
    protected abstract fun speak(context: DiscussionContext): Claim
    abstract fun watchEpilogue(chronicles: List<Recallable>)

    protected fun memorize(recallable: Recallable) {
        _memories.add(recallable)
    }

    // signal is a capability token: only callers who hold a GameOverSignal (i.e., after game over) can access memories
    @Suppress("UnusedParameter")
    fun reveal(signal: GameOverSignal): List<Recallable> = _memories.toList()

    fun buildNightAction(players: List<Player>, isFirstNight: Boolean): NightAction =
        role.buildNightAction(this, players, isFirstNight, _memories.filterIsInstance<GameEvent>())
}
