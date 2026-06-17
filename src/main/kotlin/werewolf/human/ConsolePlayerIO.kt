package werewolf.human

class ConsolePlayerIO : PlayerIO() {
    override fun sendMessage(title: String, content: String) {
        println("[$title] $content")
    }

    override fun readFreeText(): String = readLine() ?: ""
    override fun readChoice(): String = readLine() ?: ""
    override fun readPlayer(): String = readLine() ?: ""
}
