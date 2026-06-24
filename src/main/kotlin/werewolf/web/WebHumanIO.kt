package werewolf.web

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import werewolf.game.ChronicleView
import werewolf.game.GameOverSignal
import werewolf.game.RecallView
import werewolf.view.ChoiceView

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

    fun display(view: RecallView) {
        checkAbort()
        enqueue(view.toJson())
    }

    fun promptChoice(view: ChoiceView): String {
        checkAbort()
        val optionsJson = view.options.joinToString(",") { it.jsonEncode() }
        val prompt = """{"type":"choose","title":${view.title.jsonEncode()},"description":${view.description.jsonEncode()},"candidates":[$optionsJson]}"""
        enqueue(prompt)
        while (true) {
            val selected = runBlocking { incoming.receive() }
            checkAbort()
            if (selected in view.options) return selected
            enqueue(prompt)
        }
    }

    fun promptFreeText(title: String, description: String): String {
        checkAbort()
        enqueue("""{"type":"speak","title":${title.jsonEncode()},"description":${description.jsonEncode()}}""")
        val text = runBlocking { incoming.receive() }
        checkAbort()
        return text
    }

    fun watchEpilogue(chronicles: List<ChronicleView>) {
        val json = chronicles.joinToString(",", "[", "]") { it.toJson() }
        enqueue("""{"type":"epilogue","chronicles":$json}""")
        outgoing.close()
    }

    private fun enqueue(message: String) {
        check(outgoing.trySend(message).isSuccess) { "Failed to queue message" }
    }
}

private fun RecallView.toJson(): String = when (this) {
    is RecallView.Observation ->
        """{"type":"event","title":${category.jsonEncode()},"body":${content.jsonEncode()}}"""
    is RecallView.Action ->
        """{"type":"event","title":${category.jsonEncode()},"body":${content.jsonEncode()},"intent":${intent.jsonEncode()}}"""
}

private fun ChronicleView.toJson(): String = when (this) {
    is ChronicleView.Observation ->
        """{"type":"observation","recipient":${recipient.jsonEncode()}""" +
        ""","category":${category.jsonEncode()},"content":${content.jsonEncode()}}"""
    is ChronicleView.Action ->
        """{"type":"action","actor":${actor.jsonEncode()}""" +
        ""","category":${category.jsonEncode()},"content":${content.jsonEncode()}""" +
        ""","intent":${intent.jsonEncode()}}"""
}

private fun String.jsonEncode(): String =
    "\"${replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", " ")}\""
