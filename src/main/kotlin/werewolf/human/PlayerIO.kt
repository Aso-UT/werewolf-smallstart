package werewolf.human

import werewolf.game.GameOverSignal
import werewolf.game.RecallView
import werewolf.view.ChoiceView

abstract class PlayerIO {
    companion object {
        private const val ABORT_PASSWORD = 4423
    }

    abstract fun display(view: RecallView)
    abstract fun sendMessage(title: String, content: String)
    protected abstract fun readInput(): String

    fun promptFreeText(title: String, content: String): String {
        sendMessage(title, content)
        return readInput()
    }

    fun promptChoice(view: ChoiceView): String {
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
