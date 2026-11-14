package werewolf.human.web

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import werewolf.game.ChronicleView
import werewolf.game.GameOverSignal
import werewolf.game.GameRules
import werewolf.game.RecallView
import werewolf.game.Remark
import werewolf.human.HumanIO
import werewolf.view.Atmosphere
import werewolf.view.ChoiceView
import werewolf.view.EpilogueMood
import werewolf.view.GameNoteView
import werewolf.view.PlayerNote
import werewolf.view.PlayerStatus
import werewolf.view.SelectionMood

class WebHumanIO : HumanIO {
    val outgoing = Channel<String>(Channel.UNLIMITED)
    val incoming = Channel<String>(Channel.UNLIMITED)
    @Volatile private var abortRequested = false
    val rulesReading = RulesReading()

    init {
        enqueue("""{"type":"rules","text":${GameRules.description.jsonEncode()}}""")
    }

    // ルールを読んでいる間の中断も受け付けるため、読み終えるのを待っている問い合わせも進める
    fun requestAbort() {
        abortRequested = true
        rulesReading.finish()
    }

    // 中断の要求は一度だけ投げる。中断後のエピローグでも表示は続くため、要求が残っていると再び投げてしまう
    fun checkAbort() {
        if (!abortRequested) return
        abortRequested = false
        GameOverSignal.throwManualAbort()
    }

    override fun updateGameNotePanel(view: GameNoteView) {
        val playersJson = view.players.joinToString(",") { it.toJson() }
        val divinationReportersJson = view.divinationReporters.joinToString(",") { it.jsonEncode() }
        val mediumReportersJson = view.mediumReporters.joinToString(",") { it.jsonEncode() }
        enqueue(
            """{"type":"gameNote","players":[$playersJson],""" +
                """"ownResultTitle":${view.ownResultTitle?.jsonEncode() ?: "null"},""" +
                """"divinationReporters":[$divinationReportersJson],"mediumReporters":[$mediumReportersJson]}"""
        )
    }

    override fun updateAtmosphere(atmosphere: Atmosphere) {
        enqueue("""{"type":"atmosphere","value":${atmosphere.toJson()}}""")
    }

    override fun updateSelectionMood(mood: SelectionMood) {
        enqueue("""{"type":"selectionMood","value":${mood.toJson()}}""")
    }

    override fun updateEpilogueMood(mood: EpilogueMood) {
        enqueue("""{"type":"epilogueMood","value":${mood.toJson()}}""")
    }

    override fun display(view: RecallView) {
        checkAbort()
        enqueue(view.toJson())
    }

    override fun waitUntilReady() {
        rulesReading.waitUntilFinished()
    }

    override fun promptChoice(view: ChoiceView): String {
        checkAbort()
        val optionsJson = view.options.joinToString(",") { it.jsonEncode() }
        enqueue("""{"type":"choose","title":${view.title.jsonEncode()},"description":${view.description.jsonEncode()},"candidates":[$optionsJson]}""")
        val selected = runBlocking { incoming.receive() }
        checkAbort()
        check(selected in view.options) { "Unexpected choice from web client: $selected" }
        return selected
    }

    override fun promptFreeText(title: String, description: String): String {
        checkAbort()
        enqueue("""{"type":"speak","title":${title.jsonEncode()},"description":${description.jsonEncode()}}""")
        val text = runBlocking { incoming.receive() }
        checkAbort()
        return text
    }

    override fun promptSpeakingUrge(title: String, seconds: Int, buttons: List<String>, timeoutButton: String): String {
        checkAbort()
        val description = "発言したい場合は${seconds}秒以内にボタンを押してください"
        val buttonsJson = buttons.joinToString(",") { it.jsonEncode() }
        enqueue(
            """{"type":"urge","title":${title.jsonEncode()},"description":${description.jsonEncode()},""" +
                """"seconds":$seconds,"buttons":[$buttonsJson],"timeoutButton":${timeoutButton.jsonEncode()}}"""
        )
        val answer = runBlocking { incoming.receive() }
        checkAbort()
        check(answer in buttons || answer == timeoutButton) { "Unexpected urge button from web client: $answer" }
        return answer
    }

    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) {
        val chroniclesJson = chronicles.joinToString(",", "[", "]") { it.toJson() }
        val metadataTotalsJson = metadataTotals.joinToString(",", "[", "]") { it.jsonEncode() }
        enqueue("""{"type":"epilogue","chronicles":$chroniclesJson,"metadataTotals":$metadataTotalsJson}""")
        outgoing.close()
    }

    private fun enqueue(message: String) {
        check(outgoing.trySend(message).isSuccess) { "Failed to queue message" }
    }
}

private fun PlayerNote.toJson(): String =
    """{"name":${name.jsonEncode()},"status":${status.toJson()},""" +
        """"claimedRole":${claimedRole?.jsonEncode() ?: "null"},"knownRole":${knownRole?.jsonEncode() ?: "null"},""" +
        """"ownResult":${ownResult ?: "null"},""" +
        """"divinationReports":${divinationReports.toJson()},"mediumReports":${mediumReports.toJson()}}"""

// 報告した人の名前から、報告された結果（true は人狼）へのオブジェクトにする
private fun Map<String, Boolean>.toJson(): String =
    entries.joinToString(",", "{", "}") { (reporter, isWerewolf) -> "${reporter.jsonEncode()}:$isWerewolf" }

private fun PlayerStatus.toJson(): String = when (this) {
    PlayerStatus.ALIVE -> "\"alive\""
    PlayerStatus.EXECUTED -> "\"executed\""
    PlayerStatus.ATTACKED -> "\"attacked\""
}

private fun Atmosphere.toJson(): String = when (this) {
    Atmosphere.MORNING -> "\"morning\""
    Atmosphere.DAY -> "\"day\""
    Atmosphere.NIGHT -> "\"night\""
    Atmosphere.VOTE -> "\"vote\""
}

private fun SelectionMood.toJson(): String = when (this) {
    SelectionMood.ATTACK -> "\"attack\""
    SelectionMood.DIVINE -> "\"divine\""
    SelectionMood.GUARD -> "\"guard\""
    SelectionMood.VOTE -> "\"vote\""
}

private fun EpilogueMood.toJson(): String = when (this) {
    EpilogueMood.CITIZEN_WIN -> "\"citizenWin\""
    EpilogueMood.CITIZEN_LOSE -> "\"citizenLose\""
    EpilogueMood.WEREWOLF_WIN -> "\"werewolfWin\""
    EpilogueMood.WEREWOLF_LOSE -> "\"werewolfLose\""
}

private fun RecallView.toJson(): String = when (this) {
    is RecallView.Observation ->
        """{"type":"observation","title":${category.jsonEncode()},"body":${content.jsonEncode()}}"""
    is RecallView.AttributedObservation ->
        """{"type":"attributedObservation","title":${category.jsonEncode()},"actor":${actor.jsonEncode()},"body":${content.jsonEncode()}}"""
    is RecallView.SimultaneousObservation ->
        """{"type":"simultaneousObservation","title":${category.jsonEncode()},""" +
            """"remarks":${remarks.joinToString(",", "[", "]") { it.toJson() }}}"""
    is RecallView.SelfAction ->
        """{"type":"action","title":${category.jsonEncode()},"body":${content.jsonEncode()},"intent":${intent.jsonEncode()}}"""
}

private fun Remark.toJson(): String =
    """{"actor":${actor.jsonEncode()},"body":${content.jsonEncode()}}"""

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
