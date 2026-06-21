package werewolf.web

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType

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
        enqueue("""{"type":"event","title":${event.title.jsonEncode()},"body":${event.body().jsonEncode()}}""")
    }

    override fun choose(context: SelectionContext): Choice {
        checkAbort()
        val candidates = context.candidates()
        val selected = promptChoice(context.title, context.description, candidates.map { it.name })
        return Choice(this, context, candidates.first { it.name == selected }, "ブラウザ選択")
    }

    override fun speak(context: DiscussionContext): Claim {
        checkAbort()
        val type = selectStatementType(context)
        val statement = when (type) {
            StatementType.PLAIN -> buildPlain(context)
            StatementType.DIVINATION_REPORT -> buildDivinationReport(context)
            StatementType.MEDIUM_REPORT -> buildMediumReport(context)
        }
        return Claim(this, context, statement, "ブラウザ発言")
    }

    private fun selectStatementType(context: DiscussionContext): StatementType {
        val types = StatementType.entries.filter { it in context.availableTypes }
        if (types.size == 1) return types.first()
        val selected = promptChoice(context.title, context.description, types.map { it.displayName })
        return types.first { it.displayName == selected }
    }

    private fun buildPlain(context: DiscussionContext): Statement {
        enqueue("""{"type":"speak","title":${context.title.jsonEncode()},"description":"発言してください"}""")
        val text = runBlocking { incoming.receive() }
        checkAbort()
        return Statement.Plain(text)
    }

    private fun buildDivinationReport(context: DiscussionContext): Statement {
        val candidates = context.allPlayers.filter { it !== this }
        val targetName = promptChoice("占い報告 - 対象", "誰の占い結果を報告しますか？", candidates.map { it.name })
        val target = candidates.first { it.name == targetName }
        val results = DivineResult.entries
        val resultName = promptChoice("占い報告 - 結果", "占い結果を選んでください", results.map { it.displayName })
        return Statement.DivinationReport(this, target, results.first { it.displayName == resultName })
    }

    private fun buildMediumReport(context: DiscussionContext): Statement {
        val candidates = context.allPlayers.filter { it !== this }
        val targetName = promptChoice("霊媒報告 - 対象", "誰の霊媒結果を報告しますか？", candidates.map { it.name })
        val target = candidates.first { it.name == targetName }
        val results = MediumResult.entries
        val resultName = promptChoice("霊媒報告 - 結果", "霊媒結果を選んでください", results.map { it.displayName })
        return Statement.MediumReport(this, target, results.first { it.displayName == resultName })
    }

    private fun promptChoice(title: String, description: String, options: List<String>): String {
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
