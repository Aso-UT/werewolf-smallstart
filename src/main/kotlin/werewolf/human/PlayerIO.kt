package werewolf.human

import werewolf.game.Player

interface PlayerIO {
    fun sendMessage(title: String, content: String)
    fun promptPlayer(title: String, content: String, candidates: List<Player>): Player
    fun promptFreeText(title: String, content: String): String
    fun promptChoice(title: String, content: String, options: List<String>): Int
}
