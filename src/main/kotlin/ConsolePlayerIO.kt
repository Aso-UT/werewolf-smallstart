package org.example

class ConsolePlayerIO : PlayerIO {
    override fun sendMessage(playerName: String, title: String, content: String) {
        println("=== ${playerName}：${title} ===")
        println(content)
    }

    override fun promptPlayer(playerName: String, title: String, content: String, candidates: List<Player>): Player {
        while (true) {
            sendMessage(playerName, title, "$content: ${candidates.joinToString(", ") { it.name }}")
            val input = readLine() ?: ""
            val player = candidates.firstOrNull { it.name == input }
            if (player != null) return player
            println("「$input」は候補にいません。もう一度入力してください。")
        }
    }

    override fun promptFreeText(playerName: String, title: String, content: String): String {
        sendMessage(playerName, title, content)
        return readLine() ?: ""
    }

    override fun promptChoice(playerName: String, title: String, content: String, options: List<String>): Int {
        while (true) {
            sendMessage(playerName, title, content)
            options.forEachIndexed { i, option -> println("${i + 1}: $option") }
            val input = readLine()?.toIntOrNull()
            if (input != null && input in 1..options.size) return input - 1
            println("1〜${options.size}の数字を入力してください。")
        }
    }
}
