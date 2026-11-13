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

    fun compose(speaker: Player, context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Statement =
        when (selectType(context, claimableRoles, reportEligibility)) {
            StatementType.PLAIN -> composePlain(speaker, context)
            StatementType.DIVINATION_REPORT -> composeDivinationReport(speaker, reportEligibility)
            StatementType.MEDIUM_REPORT -> composeMediumReport(speaker, reportEligibility)
            StatementType.ROLE_CLAIM -> composeRoleClaim(speaker, claimableRoles)
        }

    private fun selectType(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): StatementType {
        val types = StatementType.entries.filter { it in context.selectableTypes(claimableRoles, reportEligibility) }
        return selection.select(context.title, context.description, types) { it.displayName }
    }

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

    private fun composeRoleClaim(speaker: Player, claimableRoles: Set<Role>): Statement {
        val role = selection.select("役職申告 - 役職", "申告する役職を選んでください", claimableRoles.toList()) { it.displayName }
        val comment = io.promptFreeText("役職申告 - 補足", COMMENT_PROMPT)
        return Statement.RoleClaim(speaker, role, comment)
    }
}
