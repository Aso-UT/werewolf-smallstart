package werewolf.ai

import werewolf.game.ActionMetadata
import werewolf.game.Choice
import werewolf.game.ChronicleView
import werewolf.game.Claim
import werewolf.game.CounterClaimSpeakingUrge
import werewolf.game.DiscussionContext
import werewolf.game.FallbackChoice
import werewolf.game.FallbackClaim
import werewolf.game.FallbackSpeakingUrge
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.GameRules
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.RecallView
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.SpeakingUrge
import werewolf.game.UrgeEligibility

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
        // 発言のたびにたまって入力が膨らみ、AIも解釈しにくいため記憶には入れない
        return askUntilValid(urgeLanguageModel, instruction) { buildUrge(context, urgeFormat.parse(it.text, eligibility), it.metadata) }
            ?: FallbackSpeakingUrge(this, context)
    }

    private fun buildUrge(context: DiscussionContext, parsed: ParsedUrge, metadata: ActionMetadata): SpeakingUrge =
        parsed.counterClaimRole
            ?.let { CounterClaimSpeakingUrge(this, context, it, parsed.intent, metadata) }
            ?: SpeakingUrge(this, context, parsed.eagerness, parsed.intent, metadata)

    override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
        val instruction = statementFormat.buildInstruction(context, claimableRoles, reportEligibility)
        val claim = askUntilValid(languageModel, instruction) { completion ->
            val parsed = statementFormat.parse(completion.text, this, context, claimableRoles, reportEligibility)
            Claim(
                this, context, parsed.statement, claimableRoles, reportEligibility,
                intent = parsed.intent,
                metadata = completion.metadata,
            )
        } ?: FallbackClaim(this, context)
        _myMemories.add(claim)
        return claim
    }

    override fun choose(context: SelectionContext): Choice {
        val candidates = context.candidates()
        val instruction = """
            ${context.title}：${context.description}

            候補：${candidates.joinToString("、") { it.name }}

            「選んだ理由（200文字以内）：候補名」の形式で答えてください。
            例：最も怪しいと思うため：${candidates.first().name}
        """.trimIndent()
        val choice = askUntilValid(languageModel, instruction) { completion ->
            val (target, intent) = parseChoiceResponse(completion.text, candidates)
            Choice(this, context, target, intent = intent, metadata = completion.metadata)
        } ?: FallbackChoice(this, context)
        _myMemories.add(choice)
        return choice
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

    // 脱落しても反芻は続けているので、ゲームが終わったここで止め、残りを記録する
    override fun onRevealing(signal: GameOverSignal) {
        languageModel.stop()
        urgeLanguageModel.stop()
        recordRuminations()
    }

    // 反芻はAIへの指示には入れず、振り返りの記録にだけ残す
    private fun recordRuminations() {
        (languageModel.drainRuminations() + urgeLanguageModel.drainRuminations())
            .forEach { memorize(Rumination(this, it)) }
    }

    // 応答が不正な形式なら、記録してもう一度聞き直す。最後まで正しい応答を得られなければ null を返す
    private fun <T> askUntilValid(model: LanguageModel, instruction: String, interpret: (Completion.Answer) -> T): T? {
        repeat(MAX_ATTEMPTS) {
            when (val completion = prompt(model, instruction)) {
                is Completion.Answer -> try {
                    return interpret(completion)
                } catch (_: InvalidAiInputException) {
                    memorize(InvalidAiInput(this, completion.text, completion.metadata))
                }
                is Completion.Refusal -> {
                    memorize(RefusedAiInput(this, completion.text, completion.metadata))
                    // 拒否はモデル提供元の安全分類器による判定で、同じモデルに同じプロンプトを送り直しても、ほとんどの場合また拒否される。
                    // Anthropicの公式ドキュメント（拒否とフォールバック）でも、同じモデルへの再送ではなく別モデルへの切り替えが推奨されている。
                    // 実際のゲームでも、同じプロンプトでの再試行16回中15回がまた拒否された（#353）。そのため聞き直さずに諦める
                    return null
                }
            }
        }
        return null
    }

    private fun prompt(model: LanguageModel, instruction: String): Completion {
        recordRuminations()
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

    companion object {
        private const val MAX_ATTEMPTS = 2
    }
}

