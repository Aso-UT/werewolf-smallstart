package werewolf.human.console

import werewolf.game.ChronicleView
import werewolf.game.GameOverSignal
import werewolf.game.RecallView
import werewolf.human.HumanIO
import werewolf.view.Atmosphere
import werewolf.view.ChoiceView
import werewolf.view.DivinationView
import werewolf.view.EpilogueMood
import werewolf.view.PlayerStatusView
import werewolf.view.SelectionMood

class ConsoleHumanIO : HumanIO {

    companion object {
        private const val ABORT_PASSWORD = 4423
    }

    override fun updatePlayerStatusPanel(view: PlayerStatusView) {
        // Console has no persistent panel; player status info is visible in the event log
    }

    override fun updateDivinationPanel(view: DivinationView) {
        // Console has no persistent panel; divination info is visible in the event log
    }

    override fun updateAtmosphere(atmosphere: Atmosphere) {
        // Console has no visual background; time-of-day is visible in the event log
    }

    override fun updateSelectionMood(mood: SelectionMood) {
        // Console has no visual selection panel; the choice prompt itself conveys this
    }

    override fun updateEpilogueMood(mood: EpilogueMood) {
        // Console has no visual background; the GameResult event itself conveys this
    }

    override fun display(view: RecallView) = when (view) {
        is RecallView.Observation -> println("[${view.category}] ${view.content}")
        is RecallView.AttributedObservation -> println("[${view.category}] ${view.actor}: ${view.content}")
        is RecallView.SelfAction -> println("[${view.category}] ${view.content}\n  [${view.intent}]")
    }

    private fun sendMessage(title: String, content: String) {
        println("[$title] $content")
    }

    private fun readInput(): String = readLine() ?: ""

    override fun promptFreeText(title: String, description: String): String {
        sendMessage(title, description)
        return readInput()
    }

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

    override fun watchEpilogue(chronicles: List<ChronicleView>) {
        sendMessage("ゲーム振り返り", chronicles.joinToString("\n") { it.formatForConsole() })
    }
}

private fun ChronicleView.formatForConsole(): String = when (this) {
    is ChronicleView.Observation -> "[$recipient] [$category] $content"
    is ChronicleView.Action -> {
        val line = "[$actor] [$category] $content"
        if (intent.isNotEmpty()) "$line\n  [$intent]" else line
    }
}
