package werewolf.ai

import werewolf.game.Recallable

class Instruction(private val text: String) : Recallable() {
    override fun recall() = text
    override fun chronicle() = "[指示] $text"
}
