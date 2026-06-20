package werewolf.web

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

class WebPlayer(role: Role, override val name: String) : Player(role) {
    val outgoing = Channel<String>(Channel.UNLIMITED)
    val incoming = Channel<String>(Channel.UNLIMITED)
    @Volatile private var abortRequested = false

    fun requestAbort() {
        abortRequested = true
    }

    private fun checkAbort() {
        if (abortRequested) GameOverSignal.throwManualAbort()
    }

    private fun enqueue(message: String) {
        check(outgoing.trySend(message).isSuccess) { "Failed to queue message" }
    }

    override fun onReceive(event: GameEvent) {
        checkAbort()
        enqueue("""{"type":"message","title":${event.title.jsonEncode()},"body":${event.body().jsonEncode()}}""")
    }

    override fun choose(context: SelectionContext): Choice {
        checkAbort()
        val candidatesJson = context.candidates().joinToString(",") { it.name.jsonEncode() }
        val prompt = """{"type":"choose","title":${context.title.jsonEncode()},"description":${context.description.jsonEncode()},"candidates":[$candidatesJson]}"""
        enqueue(prompt)
        while (true) {
            val selected = runBlocking { incoming.receive() }
            checkAbort()
            val player = context.candidates().find { it.name == selected }
            if (player != null) return Choice(this, context, player, "ブラウザ選択")
            enqueue(prompt)
        }
    }

    override fun speak(context: DiscussionContext): Claim {
        checkAbort()
        enqueue("""{"type":"speak","title":${context.title.jsonEncode()},"description":${context.description.jsonEncode()}}""")
        val text = runBlocking { incoming.receive() }
        checkAbort()
        return Claim(this, context, Statement.Plain(text), "ブラウザ発言")
    }

    override fun watchEpilogue(chronicles: List<Recallable>) {
        val content = chronicles
            .filter { !it.isRedundantInChronicle }
            .joinToString("\n") {
                val intent = it.intentForChronicle
                if (intent != null) "${it.chronicle()}\n  [$intent]" else it.chronicle()
            }
        enqueue("""{"type":"epilogue","content":${content.jsonEncode()}}""")
        outgoing.close()
    }

    private fun String.jsonEncode(): String =
        "\"${replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", " ")}\""
}
