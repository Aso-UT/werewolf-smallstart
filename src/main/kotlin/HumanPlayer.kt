package org.example

class HumanPlayer(override val role: Role, override val name: String, private val io: PlayerIO) : Player {
    override fun notifyRole() {
        io.sendMessage(name, "役職通知", "あなたの役職は「${role.displayName}」です。")
    }

    override fun selectTarget(context: SelectionContext): Player =
        io.prompt(name, context.title, context.description, context.candidates())

    override fun receive(event: GameEvent) {
        io.sendMessage(name, event.title, event.body(this))
    }

    override fun discuss(players: List<Player>): String =
        io.promptFreeText(name, "議論", "発言してください")
}