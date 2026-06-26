package werewolf.human.console

import werewolf.game.GameOverSignal
import werewolf.game.RecallView
import werewolf.human.PlayerIO
import werewolf.view.ChoiceView

class ConsolePlayerIO : PlayerIO() {

    companion object {
        private const val ABORT_PASSWORD = 4423
    }

    override fun display(view: RecallView) = when (view) {
        is RecallView.Observation -> println("[${view.category}] ${view.content}")
        is RecallView.Action -> println("[${view.category}] ${view.content}\n  [${view.intent}]")
    }

    override fun sendMessage(title: String, content: String) {
        println("[$title] $content")
    }

    override fun readInput(): String = readLine() ?: ""

    override fun promptChoice(view: ChoiceView): String {
        val optionsText = view.options.mapIndexed { i, option -> "${i + 1}: $option" }.joinToString("\n")
        while (true) {
            sendMessage(view.title, "${view.description}\n$optionsText")
            val number = readInput().toIntOrNull()
            if (number == ABORT_PASSWORD) GameOverSignal.throwManualAbort()
            if (number != null && number in 1..view.options.size) return view.options[number - 1]
            sendMessage(view.title, "1〜${view.options.size}の数字を入力してください。")
        }
    }
}
