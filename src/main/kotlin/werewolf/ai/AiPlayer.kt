package werewolf.ai

import werewolf.game.ActionMetadata
import werewolf.game.Choice
import werewolf.game.ChronicleView
import werewolf.game.Claim
import werewolf.game.CounterClaimSpeakingUrge
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.FallbackChoice
import werewolf.game.FallbackClaim
import werewolf.game.FallbackSpeakingUrge
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.GameRules
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.RecallView
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.SpeakingUrge
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.UrgeEligibility
import werewolf.game.selectableTypes

class AiPlayer(
    role: Role,
    override val name: String,
    private val languageModel: LanguageModel,
    // 発言意欲は発言のたびに全員へ聞くため、安価なモデルを使えるよう分けておく
    private val urgeLanguageModel: LanguageModel,
    instruction: Instruction,
) : Player(role) {
    private val roleAdvice = RoleAdvice.random(role, name)
    private val _myMemories = mutableListOf<Recallable>(instruction, roleAdvice)
    private val statementFormat = StatementFormat()
    private val urgeFormat = UrgeFormat()

    init {
        memorize(instruction)
        memorize(roleAdvice)
    }

    override fun onReceive(event: GameEvent) {
        if (isRedundantWithOwnClaim(event)) return
        _myMemories.add(event)
    }

    private fun isRedundantWithOwnClaim(event: GameEvent): Boolean = when (event) {
        is GameEvent.StatementMade -> event.speakerName == name
        is GameEvent.WerewolfStatementMade -> event.speakerName == name
        else -> false
    }

    override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility): SpeakingUrge {
        val instruction = urgeFormat.buildInstruction(context, eligibility)
        repeat(2) {
            val completion = prompt(urgeLanguageModel, instruction)
            try {
                val parsed = urgeFormat.parse(completion.text, eligibility)
                // 発言のたびにたまって入力が膨らみ、AIも解釈しにくいため記憶には入れない
                return buildUrge(context, parsed, completion.metadata)
            } catch (_: InvalidAiInputException) {
                // AI応答が不正な形式のため、記録して次のイテレーションでリトライする
                memorize(InvalidAiInput(this, completion.text, completion.metadata))
            }
        }
        return FallbackSpeakingUrge(this, context)
    }

    private fun buildUrge(context: DiscussionContext, parsed: ParsedUrge, metadata: ActionMetadata): SpeakingUrge =
        parsed.counterClaimRole
            ?.let { CounterClaimSpeakingUrge(this, context, it, parsed.intent, metadata) }
            ?: SpeakingUrge(this, context, parsed.eagerness, parsed.intent, metadata)

    override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
        val instruction = statementFormat.buildInstruction(context, claimableRoles, reportEligibility)
        repeat(2) {
            val completion = prompt(languageModel, instruction)
            try {
                val parsed = statementFormat.parse(completion.text)
                val type = context.selectableTypes(claimableRoles, reportEligibility).singleOrNull { it.displayName == parsed.typeLabel }
                    ?: throw InvalidAiInputException("選択できない発言の種類です: ${parsed.typeLabel}")
                val statement = buildStatement(context, type, parsed.content, claimableRoles)
                if (reportEligibility.disqualifies(statement)) {
                    throw InvalidAiInputException("報告可能な対象・結果ではありません: ${statement.text()}")
                }
                val claim = Claim(
                    this, context, statement, claimableRoles, reportEligibility,
                    intent = parsed.intent,
                    metadata = completion.metadata,
                )
                _myMemories.add(claim)
                return claim
            } catch (_: InvalidAiInputException) {
                // AI応答が不正な形式のため、記録して次のイテレーションでリトライする
                memorize(InvalidAiInput(this, completion.text, completion.metadata))
            }
        }
        return FallbackClaim(this, context).also { _myMemories.add(it) }
    }

    private fun buildStatement(
        context: DiscussionContext,
        type: StatementType,
        content: String,
        claimableRoles: Set<Role>,
    ): Statement = when (type) {
        StatementType.PLAIN -> Statement.Plain(this, content)
        StatementType.DIVINATION_REPORT -> {
            val (targetName, resultLabel, comment) = statementFormat.extractReportParts(content)
            val result = DivineResult.entries.singleOrNull { it.displayName == resultLabel }
                ?: throw InvalidAiInputException("占い結果報告の結果が不正です: $resultLabel")
            Statement.DivinationReport(this, resolveTarget(context, targetName), result, comment)
        }
        StatementType.MEDIUM_REPORT -> {
            val (targetName, resultLabel, comment) = statementFormat.extractReportParts(content)
            val result = MediumResult.entries.singleOrNull { it.displayName == resultLabel }
                ?: throw InvalidAiInputException("霊媒結果報告の結果が不正です: $resultLabel")
            Statement.MediumReport(this, resolveTarget(context, targetName), result, comment)
        }
        StatementType.ROLE_CLAIM -> {
            val (roleName, comment) = statementFormat.extractRoleClaimParts(content)
            val role = claimableRoles.singleOrNull { it.displayName == roleName }
                ?: throw InvalidAiInputException("役職申告の役職が不正です: $roleName")
            Statement.RoleClaim(this, role, comment)
        }
    }

    private fun resolveTarget(context: DiscussionContext, targetName: String): Player =
        context.allPlayers.singleOrNull { it.name == targetName }
            ?: throw InvalidAiInputException("報告の対象が不正です: $targetName")

    override fun choose(context: SelectionContext): Choice {
        val candidates = context.candidates()
        val instruction = """
            ${context.title}：${context.description}

            候補：${candidates.joinToString("、") { it.name }}

            「選んだ理由（200文字以内）：候補名」の形式で答えてください。
            例：最も怪しいと思うため：${candidates.first().name}
        """.trimIndent()
        repeat(2) {
            val completion = prompt(languageModel, instruction)
            try {
                val (target, intent) = parseChoiceResponse(completion.text, candidates)
                val choice = Choice(
                    this, context, target,
                    intent = intent,
                    metadata = completion.metadata,
                )
                _myMemories.add(choice)
                return choice
            } catch (_: InvalidAiInputException) {
                // AI応答が不正な形式のため、記録して次のイテレーションでリトライする
                memorize(InvalidAiInput(this, completion.text, completion.metadata))
            }
        }
        return FallbackChoice(this, context).also { _myMemories.add(it) }
    }

    private fun parseChoiceResponse(input: String, candidates: List<Player>): Pair<Player, String> {
        val separatorIdx = maxOf(input.lastIndexOf("："), input.lastIndexOf(":"))
            .takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「理由：候補名」の形式ではありません: $input")
        val intent = input.substring(0, separatorIdx).trim()
        val targetString = input.substring(separatorIdx + 1).trim()
        val target = candidates.firstOrNull { it.name == targetString }
            ?: throw InvalidAiInputException("候補に存在しないターゲットです: $targetString")
        return target to intent
    }

    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) = Unit

    private fun prompt(model: LanguageModel, instruction: String): Completion {
        val history = _myMemories.map { toHistoryEntry(it.toRecallView()) }
        val instructionText = buildString {
            appendLine("【指示】")
            append(instruction)
        }
        @Suppress("TooGenericExceptionCaught")
        return try {
            model.ask(gameDescription, history, instructionText)
        } catch (e: Exception) {
            GameOverSignal.throwAborted(e)
        }
    }

    private val gameDescription = buildString {
        appendLine("あなたはプレイヤー${name}です。あなたの役職はゲームの流れの「役職通知」をご確認ください。")
        appendLine("ゲームの流れの冒頭にある指示に従って行動してください。")
        appendLine("ゲームの流れのうち〔あなたのメモ：…〕は、あなたが行動したときに書いた理由で、あなたにしか見えません。")
        append(GameRules.description)
    }

    // Uses <> to avoid collision with AI response delimiters (: and [])
    private fun toHistoryEntry(view: RecallView): String = when (view) {
        is RecallView.Observation -> "<${view.category}> ${view.content}"
        is RecallView.AttributedObservation -> "<${view.category}> ${view.actor}: ${view.content}"
        is RecallView.SelfAction -> "<${view.category}> ${name}（あなた）: ${view.content}　〔あなたのメモ：${view.intent}〕"
    }
}

