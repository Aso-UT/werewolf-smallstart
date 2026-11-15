package werewolf.ai

import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.selectableTypes

data class ParsedStatement(val statement: Statement, val intent: String)

class StatementFormat {
    fun buildInstruction(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): String {
        val types = selectableTypes(context, claimableRoles, reportEligibility)
        val typeFormats = kindsOf(types).joinToString("\n") { "・${formatDescription(it, types, reportEligibility)}" }
        // trimIndent()はテンプレート内の複数行にまたがる補間値には対応できないため、行単位で組み立てる
        return listOf(
            "【${context.title}】${context.description}",
            "",
            "発言の種類を一つ選び、以下のいずれかの形式で発言してください。",
            typeFormats,
            "",
            "発言全体の末尾に[発言の真意（50文字以内）]を付けてください。",
            "例：${StatementType.Plain.displayName}：昨日の投票について気になる点があります[怪しい人を絞り込むため]",
            "例：${StatementType.DivinationReport.displayName}：Alice/人狼/昨日の発言が不自然でした[占い師として信頼を得るため]",
            "",
            "回答には、上記の説明文自体は含めないでください。",
        ).joinToString("\n")
    }

    private fun selectableTypes(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): List<StatementType> =
        StatementType.all.filter { it in context.selectableTypes(claimableRoles, reportEligibility) }

    // 役職COは名乗る役職ごとに種類が分かれるが、AIにはまず「役職を開示する」として一つにまとめて見せ、役職は内容で答えさせる
    private fun kindsOf(types: List<StatementType>): List<StatementType> = types.distinctBy { it.displayName }

    private fun claimableRolesIn(types: List<StatementType>): List<Role> = types.filterIsInstance<StatementType.RoleClaim>().map { it.role }

    fun parse(
        input: String,
        speaker: Player,
        context: DiscussionContext,
        claimableRoles: Set<Role>,
        reportEligibility: ReportEligibility,
    ): ParsedStatement {
        val (body, intent) = parseBodyAndIntent(input)
        val (typeLabel, content) = parseTypeLabelAndContent(body)
        val types = selectableTypes(context, claimableRoles, reportEligibility)
        val kind = kindsOf(types).matchAnswer("選択できない発言の種類です: $typeLabel") { it.displayName == typeLabel }
        val statement = buildStatement(speaker, context, kind, content, claimableRolesIn(types))
        if (reportEligibility.disqualifies(statement)) {
            throw InvalidAiInputException("報告可能な対象・結果ではありません: ${statement.text()}")
        }
        return ParsedStatement(statement, intent)
    }

    private fun buildStatement(
        speaker: Player,
        context: DiscussionContext,
        kind: StatementType,
        content: String,
        claimableRoles: List<Role>,
    ): Statement = when (kind) {
        StatementType.Plain -> Statement.Plain(speaker, content)
        StatementType.DivinationReport -> {
            val (targetName, resultLabel, comment) = extractReportParts(content)
            val result = DivineResult.entries.matchAnswer("占い結果報告の結果が不正です: $resultLabel") { it.displayName == resultLabel }
            Statement.DivinationReport(speaker, resolveTarget(context, targetName), result, comment)
        }
        StatementType.MediumReport -> {
            val (targetName, resultLabel, comment) = extractReportParts(content)
            val result = MediumResult.entries.matchAnswer("霊媒結果報告の結果が不正です: $resultLabel") { it.displayName == resultLabel }
            Statement.MediumReport(speaker, resolveTarget(context, targetName), result, comment)
        }
        is StatementType.RoleClaim -> {
            val (roleName, comment) = extractRoleClaimParts(content)
            val role = claimableRoles.matchAnswer("役職申告の役職が不正です: $roleName") { it.displayName == roleName }
            Statement.RoleClaim(speaker, role, comment)
        }
    }

    private fun resolveTarget(context: DiscussionContext, targetName: String): Player =
        context.allPlayers.matchAnswer("報告の対象が不正です: $targetName") { it.name == targetName }

    private fun parseBodyAndIntent(input: String): Pair<String, String> {
        val separatorIdx = input.indexOf("[").takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「発言[真意]」の形式ではありません: $input")
        val body = input.substring(0, separatorIdx).trim()
        val intent = input.substring(separatorIdx + 1).removeSuffix("]").trim()
        return body to intent
    }

    private fun parseTypeLabelAndContent(body: String): Pair<String, String> {
        val labelSeparatorIdx = body.indexOf("：").takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「発言の種類：内容」の形式ではありません: $body")
        val typeLabel = body.substring(0, labelSeparatorIdx).trim()
        val content = body.substring(labelSeparatorIdx + 1).trim()
        return typeLabel to content
    }

    private fun formatDescription(kind: StatementType, types: List<StatementType>, reportEligibility: ReportEligibility): String = when (kind) {
        StatementType.Plain -> "${kind.displayName}：ゲーム上の発言（50文字以内）"
        StatementType.DivinationReport -> divinationReportFormat(reportEligibility)
        StatementType.MediumReport -> mediumReportFormat(reportEligibility)
        is StatementType.RoleClaim ->
            "${kind.displayName}：申告する役職名（${claimableRolesIn(types).joinToString("か") { it.displayName }}）/補足コメント（省略可）"
    }

    private fun divinationReportFormat(reportEligibility: ReportEligibility): String = when (reportEligibility) {
        is ReportEligibility.Honest -> {
            val reportable = reportEligibility.divinations.entries.joinToString("、") { "${it.key.name}は${it.value.displayName}" }
            "${StatementType.DivinationReport.displayName}：対象のプレイヤー名/実際の占い結果/補足コメント（省略可）。報告できる対象と結果：$reportable"
        }
        is ReportEligibility.CanLie ->
            "${StatementType.DivinationReport.displayName}：対象のプレイヤー名/結果（${
                DivineResult.entries.joinToString("か") { it.displayName }
            }）/補足コメント（省略可）"
    }

    private fun mediumReportFormat(reportEligibility: ReportEligibility): String = when (reportEligibility) {
        is ReportEligibility.Honest -> {
            val reportable = reportEligibility.mediums.entries.joinToString("、") { "${it.key.name}は${it.value.displayName}" }
            "${StatementType.MediumReport.displayName}：対象のプレイヤー名/実際の霊媒結果/補足コメント（省略可）。報告できる対象と結果：$reportable"
        }
        is ReportEligibility.CanLie ->
            "${StatementType.MediumReport.displayName}：対象のプレイヤー名/結果（${
                MediumResult.entries.joinToString("か") { it.displayName }
            }）/補足コメント（省略可）"
    }

    private fun extractReportParts(content: String): Triple<String, String, String> {
        val parts = content.split("/")
        val targetName = parts.getOrNull(0)?.trim()
        if (targetName.isNullOrEmpty()) throw InvalidAiInputException("報告の形式が不正です: $content")
        val resultLabel = parts.getOrNull(1)?.trim()
            ?: throw InvalidAiInputException("報告の形式が不正です: $content")
        return Triple(targetName, resultLabel, parts.getOrNull(2)?.trim().orEmpty())
    }

    private fun extractRoleClaimParts(content: String): Pair<String, String> {
        val parts = content.split("/")
        val roleName = parts.getOrNull(0)?.trim()
        if (roleName.isNullOrEmpty()) throw InvalidAiInputException("役職申告の形式が不正です: $content")
        return roleName to parts.getOrNull(1)?.trim().orEmpty()
    }
}
