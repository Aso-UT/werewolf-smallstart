package werewolf.human

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType

class HumanPlayer(role: Role, override val name: String, private val io: PlayerIO) : Player(role) {
    override fun choose(context: SelectionContext): Choice =
        Choice(this, context, io.promptPlayer(context.title, context.description, context.candidates()), "プレイヤーが選択")

    override fun onReceive(event: GameEvent) {
        io.sendMessage(event.title, event.body())
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
        val idx = io.promptChoice(context.title, context.description, types.map { it.displayName })
        return types[idx]
    }

    private fun buildPlain(context: DiscussionContext): Statement =
        Statement.Plain(io.promptFreeText(context.title, "発言してください"))

    private fun buildDivinationReport(context: DiscussionContext): Statement {
        val target = io.promptPlayer("占い報告 - 対象", "誰の占い結果を報告しますか？", context.allPlayers.filter { it !== this })
        val results = DivineResult.entries
        val resultIdx = io.promptChoice("占い報告 - 結果", "占い結果を選んでください", results.map { it.displayName })
        return Statement.DivinationReport(this, target, results[resultIdx])
    }

    private fun buildMediumReport(context: DiscussionContext): Statement {
        val target = io.promptPlayer("霊媒報告 - 対象", "誰の霊媒結果を報告しますか？", context.allPlayers.filter { it !== this })
        val results = MediumResult.entries
        val resultIdx = io.promptChoice("霊媒報告 - 結果", "霊媒結果を選んでください", results.map { it.displayName })
        return Statement.MediumReport(this, target, results[resultIdx])
    }

    override fun watchEpilogue(chronicles: List<Recallable>) {
        val content = chronicles.joinToString("\n") { "  ${it.chronicle()}" }
        io.sendMessage("ゲーム振り返り", content)
    }
}
