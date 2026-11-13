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
import werewolf.game.SpeakingUrge
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.UrgeEligibility
import werewolf.game.selectableTypes
import kotlin.time.measureTimedValue

class HumanPlayer(role: Role, override val name: String, private val io: HumanIO) : Player(role) {
    companion object {
        private const val COMMENT_PROMPT = "補足があれば入力してください"
        private const val URGE_SECONDS = 5
    }

    private val screen = HumanScreen(io)
    private val selection = HumanSelection(io)

    override fun choose(context: SelectionContext): Choice {
        screen.showSelectionMood(context)
        val (selected, metadata) = measured(HumanActionKind.CHOICE) {
            selection.select(context.title, context.description, context.candidates()) { it.name }
        }
        val choice = Choice(this, context, selected, "プレイヤーが選択", metadata)
        io.display(choice.toRecallView())
        return choice
    }

    override fun onReceive(event: GameEvent) {
        screen.show(event)
    }

    override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility): SpeakingUrge {
        val buttons = UrgeButton.available(eligibility)
        val (pressed, metadata) = measured(HumanActionKind.SPEAKING_URGE) {
            io.pressedUrgeButton(context.title, URGE_SECONDS, buttons)
        }
        return pressed.press(this, context, metadata)
    }

    override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
        val (statement, metadata) = measured(HumanActionKind.STATEMENT) {
            buildStatement(context, claimableRoles, reportEligibility)
        }
        return Claim(this, context, statement, claimableRoles, reportEligibility, "プレイヤーが発言", metadata)
    }

    // 問い合わせに答えられるようになってから、行動が決まるまでの時間を測る
    private fun <T> measured(kind: HumanActionKind, decide: () -> T): Pair<T, HumanActionMetadata> {
        io.waitUntilReady()
        val (decided, responseTime) = measureTimedValue(decide)
        return decided to HumanActionMetadata(kind, responseTime)
    }

    private fun buildStatement(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Statement =
        when (selectType(context, claimableRoles, reportEligibility)) {
            StatementType.PLAIN -> buildPlain(context)
            StatementType.DIVINATION_REPORT -> buildDivinationReport(reportEligibility)
            StatementType.MEDIUM_REPORT -> buildMediumReport(reportEligibility)
            StatementType.ROLE_CLAIM -> buildRoleClaim(claimableRoles)
        }

    private fun selectType(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): StatementType {
        val types = StatementType.entries.filter { it in context.selectableTypes(claimableRoles, reportEligibility) }
        return selection.select(context.title, context.description, types) { it.displayName }
    }

    private fun buildPlain(context: DiscussionContext): Statement =
        Statement.Plain(this, io.promptFreeText(context.title, "発言してください"))

    private fun buildDivinationReport(reportEligibility: ReportEligibility): Statement {
        val target = selection.select("占い報告 - 対象", "誰の占い結果を報告しますか？", reportEligibility.divinationCandidates()) { it.name }
        val result = selection.select("占い報告 - 結果", "占い結果を選んでください", reportEligibility.divinationResults(target)) { it.displayName }
        val comment = io.promptFreeText("占い報告 - 補足", COMMENT_PROMPT)
        return Statement.DivinationReport(this, target, result, comment)
    }

    private fun buildMediumReport(reportEligibility: ReportEligibility): Statement {
        val target = selection.select("霊媒報告 - 対象", "誰の霊媒結果を報告しますか？", reportEligibility.mediumCandidates()) { it.name }
        val result = selection.select("霊媒報告 - 結果", "霊媒結果を選んでください", reportEligibility.mediumResults(target)) { it.displayName }
        val comment = io.promptFreeText("霊媒報告 - 補足", COMMENT_PROMPT)
        return Statement.MediumReport(this, target, result, comment)
    }

    private fun buildRoleClaim(claimableRoles: Set<Role>): Statement {
        val role = selection.select("役職申告 - 役職", "申告する役職を選んでください", claimableRoles.toList()) { it.displayName }
        val comment = io.promptFreeText("役職申告 - 補足", COMMENT_PROMPT)
        return Statement.RoleClaim(this, role, comment)
    }

    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) {
        io.watchEpilogue(chronicles, metadataTotals)
    }
}
