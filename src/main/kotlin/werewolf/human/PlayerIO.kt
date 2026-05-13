package werewolf.human

import werewolf.game.Player

interface PlayerIO {
    fun sendMessage(playerName: String, title: String, content: String)
    fun promptPlayer(playerName: String, title: String, content: String, candidates: List<Player>): Player
    fun promptFreeText(playerName: String, title: String, content: String): String
    fun promptChoice(playerName: String, title: String, content: String, options: List<String>): Int
}
