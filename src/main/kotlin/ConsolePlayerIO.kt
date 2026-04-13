package org.example

class ConsolePlayerIO : PlayerIO {
    override fun sendMessage(playerName: String, title: String, content: String) {
        println("=== ${playerName}：${title} ===")
        println(content)
    }

    override fun prompt(playerName: String, title: String, content: String, candidates: List<Player>): Player {
        while (true) {
            sendMessage(playerName, title, "$content: ${candidates.joinToString(", ") { it.name }}")
            val input = readLine() ?: ""
            val player = candidates.firstOrNull { it.name == input }
            if (player != null) return player
            println("「$input」は候補にいません。もう一度入力してください。")
        }
    }
}