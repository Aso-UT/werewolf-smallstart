package werewolf.human

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.TimeOfDay
import werewolf.game.selectableTypes
import werewolf.view.Atmosphere
import werewolf.view.ChoiceView
import werewolf.view.DivinationView
import werewolf.view.PlayerStatusView

class HumanPlayer(role: Role, override val name: String, private val io: HumanIO) : Player(role) {
    companion object {
        private const val COMMENT_PROMPT = "補足があれば入力してください"
    }

    private val gameNote = GameNote()
    private var lastPlayerStatus: PlayerStatusView? = null
    private var lastDivinationSummary: DivinationView? = null

    override fun choose(context: SelectionContext): Choice {
        val selected = select(context.title, context.description, context.candidates()) { it.name }
        val choice = Choice(this, context, selected, "プレイヤーが選択")
        io.display(choice.toRecallView())
        return choice
    }

    override fun onReceive(event: GameEvent) {
        io.display(event.toRecallView())
        gameNote.post(event)
        val current = gameNote.playerStatusSummary()
        if (current != lastPlayerStatus) {
            lastPlayerStatus = current
            io.updatePlayerStatusPanel(current)
        }
        val currentDivination = gameNote.divinationSummary()
        if (currentDivination != lastDivinationSummary) {
            lastDivinationSummary = currentDivination
            io.updateDivinationPanel(currentDivination)
        }
        atmosphereOf(event)?.let { io.updateAtmosphere(it) }
    }

    private fun atmosphereOf(event: GameEvent): Atmosphere? = when (event) {
        is GameEvent.TimeChanged -> when (event.timeOfDay) {
            TimeOfDay.Morning -> Atmosphere.MORNING
            is TimeOfDay.Night -> Atmosphere.NIGHT
        }
        is GameEvent.DiscussionStarted -> Atmosphere.DAY
        is GameEvent.VoteStarted -> Atmosphere.VOTE
        else -> null
    }

    override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
        val type = selectType(context, claimableRoles, reportEligibility)
        val statement = when (type) {
            StatementType.PLAIN -> buildPlain(context)
            StatementType.DIVINATION_REPORT -> buildDivinationReport(reportEligibility)
            StatementType.MEDIUM_REPORT -> buildMediumReport(reportEligibility)
            StatementType.ROLE_CLAIM -> buildRoleClaim(claimableRoles)
        }
        return Claim(this, context, statement, claimableRoles, reportEligibility, "プレイヤーが発言")
    }

    private fun selectType(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): StatementType {
        val types = StatementType.entries.filter { it in context.selectableTypes(claimableRoles, reportEligibility) }
        return select(context.title, context.description, types) { it.displayName }
    }

    private fun buildPlain(context: DiscussionContext): Statement =
        Statement.Plain(this, io.promptFreeText(context.title, "発言してください"))

    private fun buildDivinationReport(reportEligibility: ReportEligibility): Statement {
        val target = select("占い報告 - 対象", "誰の占い結果を報告しますか？", reportEligibility.divinationCandidates()) { it.name }
        val result = select("占い報告 - 結果", "占い結果を選んでください", reportEligibility.divinationResults(target)) { it.displayName }
        val comment = io.promptFreeText("占い報告 - 補足", COMMENT_PROMPT)
        return Statement.DivinationReport(this, target, result, comment)
    }

    private fun buildMediumReport(reportEligibility: ReportEligibility): Statement {
        val target = select("霊媒報告 - 対象", "誰の霊媒結果を報告しますか？", reportEligibility.mediumCandidates()) { it.name }
        val result = select("霊媒報告 - 結果", "霊媒結果を選んでください", reportEligibility.mediumResults(target)) { it.displayName }
        val comment = io.promptFreeText("霊媒報告 - 補足", COMMENT_PROMPT)
        return Statement.MediumReport(this, target, result, comment)
    }

    private fun buildRoleClaim(claimableRoles: Set<Role>): Statement {
        val role = select("役職申告 - 役職", "申告する役職を選んでください", claimableRoles.toList()) { it.displayName }
        val comment = io.promptFreeText("役職申告 - 補足", COMMENT_PROMPT)
        return Statement.RoleClaim(this, role, comment)
    }

    private fun <T> select(title: String, description: String, candidates: List<T>, label: (T) -> String): T =
        if (candidates.size == 1) candidates.single() else promptChoice(title, description, candidates, label)

    private fun <T> promptChoice(title: String, description: String, candidates: List<T>, label: (T) -> String): T {
        val selected = io.promptChoice(ChoiceView(title, description, candidates.map(label)))
        return candidates.single { label(it) == selected }
    }

    override fun watchEpilogue(chronicles: List<ChronicleView>) {
        io.watchEpilogue(chronicles)
    }
}
