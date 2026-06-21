package werewolf.web

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import werewolf.game.GameOverSignal

class WebHumanIO {
    val outgoing = Channel<String>(Channel.UNLIMITED)
    val incoming = Channel<String>(Channel.UNLIMITED)
    @Volatile private var abortRequested = false

    fun requestAbort() {
        abortRequested = true
    }

    fun checkAbort() {
        if (abortRequested) GameOverSignal.throwManualAbort()
    }

    fun sendMessage(title: String, content: String) {
        enqueue("""{"type":"event","title":${title.jsonEncode()},"body":${content.jsonEncode()}}""")
    }

    fun promptChoice(title: String, description: String, options: List<String>): String {
        val optionsJson = options.joinToString(",") { it.jsonEncode() }
        val prompt = """{"type":"choose","title":${title.jsonEncode()},"description":${description.jsonEncode()},"candidates":[$optionsJson]}"""
        enqueue(prompt)
        while (true) {
            val selected = runBlocking { incoming.receive() }
            checkAbort()
            if (selected in options) return selected
            enqueue(prompt)
        }
    }

    fun promptFreeText(title: String, description: String): String {
        enqueue("""{"type":"speak","title":${title.jsonEncode()},"description":${description.jsonEncode()}}""")
        val text = runBlocking { incoming.receive() }
        checkAbort()
        return text
    }

    private fun enqueue(message: String) {
        check(outgoing.trySend(message).isSuccess) { "Failed to queue message" }
    }
}

private fun String.jsonEncode(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", " ")}\""
