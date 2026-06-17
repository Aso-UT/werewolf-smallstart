package werewolf.human

import werewolf.game.GameOverSignal
import werewolf.game.Player

abstract class PlayerIO {
    companion object {
        private const val ABORT_PASSWORD = 4423
    }

    abstract fun sendMessage(title: String, content: String)
    protected abstract fun readFreeText(): String
    protected abstract fun readChoice(): String
    protected abstract fun readPlayer(): String

    fun promptFreeText(title: String, content: String): String {
        sendMessage(title, content)
        return readFreeText()
    }

    fun promptPlayer(title: String, content: String, candidates: List<Player>): Player {
        while (true) {
            sendMessage(title, "$content: ${candidates.joinToString(", ") { it.name }}")
            val input = readPlayer()
            val player = candidates.firstOrNull { it.name == input }
            if (player != null) return player
            sendMessage(title, "「$input」は候補にいません。もう一度入力してください。")
        }
    }

    fun promptChoice(title: String, content: String, options: List<String>): Int {
        val optionsText = options.mapIndexed { i, option -> "${i + 1}: $option" }.joinToString("\n")
        while (true) {
            sendMessage(title, "$content\n$optionsText")
            val input = readChoice().toIntOrNull()
            if (input == ABORT_PASSWORD) GameOverSignal.throwManualAbort()
            if (input != null && input in 1..options.size) return input - 1
            sendMessage(title, "1〜${options.size}の数字を入力してください。")
        }
    }
}
