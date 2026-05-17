package werewolf.ai

import werewolf.game.Recallable

class Instruction(private val recipientName: String, private val text: String) : Recallable() {
    override fun recall() = text
    override fun chronicle() = "[$recipientName] [指示] $text"
}
