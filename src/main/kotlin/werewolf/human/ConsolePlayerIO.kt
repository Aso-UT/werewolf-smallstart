package werewolf.human

import werewolf.game.GameOverSignal
import werewolf.game.Player

class ConsolePlayerIO : PlayerIO {
    companion object {
        private const val ABORT_PASSWORD = 4423
    }

    override fun sendMessage(title: String, content: String) {
        println("[$title] $content")
    }

    override fun promptPlayer(title: String, content: String, candidates: List<Player>): Player {
        while (true) {
            sendMessage(title, "$content: ${candidates.joinToString(", ") { it.name }}")
            val input = readLine() ?: ""
            val player = candidates.firstOrNull { it.name == input }
            if (player != null) return player
            println("「$input」は候補にいません。もう一度入力してください。")
        }
    }

    override fun promptFreeText(title: String, content: String): String {
        sendMessage(title, content)
        return readLine() ?: ""
    }

    override fun promptChoice(title: String, content: String, options: List<String>): Int {
        while (true) {
            sendMessage(title, content)
            options.forEachIndexed { i, option -> println("${i + 1}: $option") }
            val input = readLine()?.toIntOrNull()
            if (input == ABORT_PASSWORD) GameOverSignal.throwManualAbort()
            if (input != null && input in 1..options.size) return input - 1
            println("1〜${options.size}の数字を入力してください。")
        }
    }
}
