package org.example

interface PlayerIO {
    fun sendMessage(playerName: String, title: String, content: String)
    fun prompt(playerName: String, title: String, content: String, candidates: List<Player>): Player
    fun promptText(playerName: String, title: String, content: String): String
}
