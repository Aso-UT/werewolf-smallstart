package werewolf.human

import werewolf.game.DiscussionContext
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.selectableTypes

class StatementComposer(private val io: HumanPrompt, private val selection: HumanSelection) {
    companion object {
        private const val COMMENT_PROMPT = "補足があれば入力してください"
    }

    fun compose(speaker: Player, context: DiscussionContext, reportEligibility: ReportEligibility): Statement {
        val types = StatementType.all.filter { it in context.selectableTypes(reportEligibility) }
        return when (selectType(context, types)) {
            StatementType.Plain -> composePlain(speaker, context)
            StatementType.DivinationReport -> composeDivinationReport(speaker, reportEligibility)
            StatementType.MediumReport -> composeMediumReport(speaker, reportEligibility)
            is StatementType.RoleClaim -> composeRoleClaim(speaker, types.filterIsInstance<StatementType.RoleClaim>().map { it.role })
        }
    }

    // 役職COは、まず役職COすることを選び、名乗る役職はその後で選ぶ
    private fun selectType(context: DiscussionContext, types: List<StatementType>): StatementType =
        selection.select(context.title, context.description, types.distinctBy { it.displayName }) { it.displayName }

    private fun composePlain(speaker: Player, context: DiscussionContext): Statement =
        Statement.Plain(speaker, io.promptFreeText(context.title, "発言してください"))

    private fun composeDivinationReport(speaker: Player, reportEligibility: ReportEligibility): Statement {
        val target = selection.select("占い報告 - 対象", "誰の占い結果を報告しますか？", reportEligibility.divinationCandidates()) { it.name }
        val result = selection.select("占い報告 - 結果", "占い結果を選んでください", reportEligibility.divinationResults(target)) { it.displayName }
        val comment = io.promptFreeText("占い報告 - 補足", COMMENT_PROMPT)
        return Statement.DivinationReport(speaker, target, result, comment)
    }

    private fun composeMediumReport(speaker: Player, reportEligibility: ReportEligibility): Statement {
        val target = selection.select("霊媒報告 - 対象", "誰の霊媒結果を報告しますか？", reportEligibility.mediumCandidates()) { it.name }
        val result = selection.select("霊媒報告 - 結果", "霊媒結果を選んでください", reportEligibility.mediumResults(target)) { it.displayName }
        val comment = io.promptFreeText("霊媒報告 - 補足", COMMENT_PROMPT)
        return Statement.MediumReport(speaker, target, result, comment)
    }

    private fun composeRoleClaim(speaker: Player, claimableRoles: List<Role>): Statement {
        val role = selection.select("役職申告 - 役職", "申告する役職を選んでください", claimableRoles) { it.displayName }
        val comment = io.promptFreeText("役職申告 - 補足", COMMENT_PROMPT)
        return Statement.RoleClaim(speaker, role, comment)
    }
}
