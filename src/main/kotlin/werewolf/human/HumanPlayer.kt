package werewolf.human

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.view.ChoiceView

class HumanPlayer(role: Role, override val name: String, private val io: PlayerIO) : Player(role) {
    override fun choose(context: SelectionContext): Choice {
        val candidates = context.candidates()
        val selected = io.promptChoice(ChoiceView(context.title, context.description, candidates.map { it.name }))
        return Choice(this, context, candidates.single { it.name == selected }, "プレイヤーが選択")
    }

    override fun onReceive(event: GameEvent) {
        io.display(event.toRecallView())
    }

    override fun speak(context: DiscussionContext): Claim {
        val type = selectType(context)
        val statement = when (type) {
            StatementType.PLAIN -> buildPlain(context)
            StatementType.DIVINATION_REPORT -> buildDivinationReport(context)
            StatementType.MEDIUM_REPORT -> buildMediumReport(context)
        }
        return Claim(this, context, statement, "プレイヤーが発言")
    }

    private fun selectType(context: DiscussionContext): StatementType {
        val types = StatementType.entries.filter { it in context.availableTypes }
        if (types.size == 1) return types.first()
        val selected = io.promptChoice(ChoiceView(context.title, context.description, types.map { it.displayName }))
        return types.single { it.displayName == selected }
    }

    private fun buildPlain(context: DiscussionContext): Statement =
        Statement.Plain(io.promptFreeText(context.title, "発言してください"))

    private fun buildDivinationReport(context: DiscussionContext): Statement {
        val candidates = context.allPlayers.filter { it !== this }
        val targetName = io.promptChoice(ChoiceView("占い報告 - 対象", "誰の占い結果を報告しますか？", candidates.map { it.name }))
        val target = candidates.single { it.name == targetName }
        val results = DivineResult.entries
        val resultName = io.promptChoice(ChoiceView("占い報告 - 結果", "占い結果を選んでください", results.map { it.displayName }))
        return Statement.DivinationReport(this, target, results.single { it.displayName == resultName })
    }

    private fun buildMediumReport(context: DiscussionContext): Statement {
        val candidates = context.allPlayers.filter { it !== this }
        val targetName = io.promptChoice(ChoiceView("霊媒報告 - 対象", "誰の霊媒結果を報告しますか？", candidates.map { it.name }))
        val target = candidates.single { it.name == targetName }
        val results = MediumResult.entries
        val resultName = io.promptChoice(ChoiceView("霊媒報告 - 結果", "霊媒結果を選んでください", results.map { it.displayName }))
        return Statement.MediumReport(this, target, results.single { it.displayName == resultName })
    }

    override fun watchEpilogue(chronicles: List<ChronicleView>) {
        io.sendMessage("ゲーム振り返り", chronicles.joinToString("\n") { it.formatForConsole() })
    }
}

private fun ChronicleView.formatForConsole(): String = when (this) {
    is ChronicleView.Observation -> "[$recipient] [$category] $content"
    is ChronicleView.Action -> {
        val line = "[$actor] [$category] $content"
        if (intent.isNotEmpty()) "$line\n  [$intent]" else line
    }
}
