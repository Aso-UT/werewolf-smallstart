package werewolf.ai

import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.Statement
import werewolf.game.StatementType

class StatementFormat(private val speaker: Player) {
    fun buildInstruction(context: DiscussionContext): String {
        val types = availableTypes(context)
        if (types.size == 1) {
            return """
                【${context.title}】${context.description}

                以下の形式で発言してください。
                ゲーム上の発言（50文字以内）[発言の真意（50文字以内）]
                例：占い師です。Aliceは白でした。[狂人として占い師を偽装し、信用を得るための発言]
                回答には、「ゲーム上の発言」などのプロンプト文字列は含めないでください。
            """.trimIndent()
        }
        val candidates = context.allPlayers.filter { it !== speaker }.joinToString("、") { it.name }
        val formatLines = types.joinToString("\n") { "・${statementFormatLine(it)}" }
        return """
            【${context.title}】${context.description}

            発言の種類を一つ選び、以下のいずれかの形式で発言してください。
            $formatLines

            対象に指定できるプレイヤー：$candidates
            発言全体の末尾に[発言の真意（50文字以内）]を付けてください。
            例：${StatementType.DIVINATION_REPORT.displayName}：Alice/人狼/昨日の発言が不自然でした[占い師として信頼を得るため]

            回答には、上記の説明文自体は含めないでください。
        """.trimIndent()
    }

    private fun availableTypes(context: DiscussionContext): List<StatementType> =
        StatementType.entries.filter { it in context.availableTypes }

    fun parse(input: String, context: DiscussionContext): Pair<Statement, String> {
        val separatorIdx = input.indexOf("[").takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「発言[真意]」の形式ではありません: $input")
        val body = input.substring(0, separatorIdx).trim()
        val intent = input.substring(separatorIdx + 1).removeSuffix("]").trim()
        val types = availableTypes(context)
        val statement = if (types.size == 1) Statement.Plain(body) else buildTypedStatement(context, body, types)
        return statement to intent
    }

    private fun statementFormatLine(type: StatementType): String = when (type) {
        StatementType.PLAIN -> "${type.displayName}：ゲーム上の発言（50文字以内）"
        StatementType.DIVINATION_REPORT ->
            "${type.displayName}：対象のプレイヤー名/結果（${DivineResult.entries.joinToString("か") { it.displayName }}）/補足コメント（省略可）"
        StatementType.MEDIUM_REPORT ->
            "${type.displayName}：対象のプレイヤー名/結果（${MediumResult.entries.joinToString("か") { it.displayName }}）/補足コメント（省略可）"
    }

    private fun buildTypedStatement(context: DiscussionContext, body: String, types: List<StatementType>): Statement {
        val labelSeparatorIdx = body.indexOf("：").takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「発言の種類：内容」の形式ではありません: $body")
        val typeLabel = body.substring(0, labelSeparatorIdx).trim()
        val content = body.substring(labelSeparatorIdx + 1).trim()
        val type = types.singleOrNull { it.displayName == typeLabel }
            ?: throw InvalidAiInputException("選択できない発言の種類です: $typeLabel")
        return when (type) {
            StatementType.PLAIN -> Statement.Plain(content)
            StatementType.DIVINATION_REPORT -> buildDivinationReport(context, content)
            StatementType.MEDIUM_REPORT -> buildMediumReport(context, content)
        }
    }

    private fun buildDivinationReport(context: DiscussionContext, content: String): Statement.DivinationReport {
        val parts = content.split("/")
        val target = resolveTarget(context, parts.getOrNull(0)?.trim(), content)
        val resultLabel = parts.getOrNull(1)?.trim()
            ?: throw InvalidAiInputException("占い結果報告の形式が不正です: $content")
        val result = DivineResult.entries.singleOrNull { it.displayName == resultLabel }
            ?: throw InvalidAiInputException("占い結果報告の結果が不正です: $resultLabel")
        return Statement.DivinationReport(speaker, target, result, parts.getOrNull(2)?.trim().orEmpty())
    }

    private fun buildMediumReport(context: DiscussionContext, content: String): Statement.MediumReport {
        val parts = content.split("/")
        val target = resolveTarget(context, parts.getOrNull(0)?.trim(), content)
        val resultLabel = parts.getOrNull(1)?.trim()
            ?: throw InvalidAiInputException("霊媒結果報告の形式が不正です: $content")
        val result = MediumResult.entries.singleOrNull { it.displayName == resultLabel }
            ?: throw InvalidAiInputException("霊媒結果報告の結果が不正です: $resultLabel")
        return Statement.MediumReport(speaker, target, result, parts.getOrNull(2)?.trim().orEmpty())
    }

    private fun resolveTarget(context: DiscussionContext, targetName: String?, content: String): Player {
        if (targetName.isNullOrEmpty()) throw InvalidAiInputException("報告の形式が不正です: $content")
        return context.allPlayers.singleOrNull { it.name == targetName && it !== speaker }
            ?: throw InvalidAiInputException("報告の対象が不正です: $targetName")
    }
}
