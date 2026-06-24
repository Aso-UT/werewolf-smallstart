package werewolf.human.console

import werewolf.game.RecallView
import werewolf.human.PlayerIO

class ConsolePlayerIO : PlayerIO() {
    override fun display(view: RecallView) = when (view) {
        is RecallView.Observation -> println("[${view.category}] ${view.content}")
        is RecallView.Action -> println("[${view.category}] ${view.content}\n  [${view.intent}]")
    }

    override fun sendMessage(title: String, content: String) {
        println("[$title] $content")
    }

    override fun readInput(): String = readLine() ?: ""
}
