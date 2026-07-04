package werewolf.ai

import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.MediumResult
import werewolf.game.Role
import werewolf.game.StatementType

data class ParsedStatement(val content: String, val typeLabel: String, val intent: String)

class StatementFormat {
    fun buildInstruction(context: DiscussionContext): String {
        val types = availableTypes(context)
        val typeFormats = types.joinToString("\n") { "・${formatDescription(it)}" }
        // trimIndent()はテンプレート内の複数行にまたがる補間値には対応できないため、行単位で組み立てる
        return listOf(
            "【${context.title}】${context.description}",
            "",
            "発言の種類を一つ選び、以下のいずれかの形式で発言してください。",
            typeFormats,
            "",
            "発言全体の末尾に[発言の真意（50文字以内）]を付けてください。",
            "例：${StatementType.PLAIN.displayName}：昨日の投票について気になる点があります[怪しい人を絞り込むため]",
            "例：${StatementType.DIVINATION_REPORT.displayName}：Alice/人狼/昨日の発言が不自然でした[占い師として信頼を得るため]",
            "",
            "回答には、上記の説明文自体は含めないでください。",
        ).joinToString("\n")
    }

    private fun availableTypes(context: DiscussionContext): List<StatementType> =
        StatementType.entries.filter { it in context.availableTypes }

    fun parse(input: String): ParsedStatement {
        val (body, intent) = parseBodyAndIntent(input)
        val (typeLabel, content) = parseTypeLabelAndContent(body)
        return ParsedStatement(content, typeLabel, intent)
    }

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

    private fun formatDescription(type: StatementType): String = when (type) {
        StatementType.PLAIN -> "${type.displayName}：ゲーム上の発言（50文字以内）"
        StatementType.DIVINATION_REPORT ->
            "${type.displayName}：対象のプレイヤー名/結果（${DivineResult.entries.joinToString("か") { it.displayName }}）/補足コメント（省略可）"
        StatementType.MEDIUM_REPORT ->
            "${type.displayName}：対象のプレイヤー名/結果（${MediumResult.entries.joinToString("か") { it.displayName }}）/補足コメント（省略可）"
        StatementType.ROLE_CLAIM ->
            "${type.displayName}：申告する役職名（${Role.entries.joinToString("か") { it.displayName }}）/補足コメント（省略可）"
    }

    fun extractReportParts(content: String): Triple<String, String, String> {
        val parts = content.split("/")
        val targetName = parts.getOrNull(0)?.trim()
        if (targetName.isNullOrEmpty()) throw InvalidAiInputException("報告の形式が不正です: $content")
        val resultLabel = parts.getOrNull(1)?.trim()
            ?: throw InvalidAiInputException("報告の形式が不正です: $content")
        return Triple(targetName, resultLabel, parts.getOrNull(2)?.trim().orEmpty())
    }

    fun extractRoleClaimParts(content: String): Pair<String, String> {
        val parts = content.split("/")
        val roleName = parts.getOrNull(0)?.trim()
        if (roleName.isNullOrEmpty()) throw InvalidAiInputException("役職申告の形式が不正です: $content")
        return roleName to parts.getOrNull(1)?.trim().orEmpty()
    }
}
