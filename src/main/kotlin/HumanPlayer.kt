package org.example

class HumanPlayer(role: Role, override val name: String, private val io: PlayerIO) : Player(role) {
    override fun selectTarget(context: SelectionContext): Player =
        io.prompt(name, context.title, context.description, context.candidates())

    override fun onReceive(event: GameEvent) {
        io.sendMessage(name, event.title, event.body())
    }

    override fun discuss(players: List<Player>): Statement =
        Statement.Plain(io.promptFreeText(name, "議論", "発言してください"))
}