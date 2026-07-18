package werewolf.ai

import werewolf.game.Choice
import werewolf.game.ChronicleView
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.DivineResult
import werewolf.game.FallbackChoice
import werewolf.game.FallbackClaim
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.MediumResult
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.RecallView
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement
import werewolf.game.StatementType
import werewolf.game.selectableTypes

class AiPlayer(
    role: Role,
    override val name: String,
    private val languageModel: LanguageModel,
    instruction: Instruction,
) : Player(role) {
    private val roleAdvice = RoleAdvice.random(role, name)
    private val _myMemories = mutableListOf<Recallable>(instruction, roleAdvice)
    private val statementFormat = StatementFormat()

    init {
        memorize(instruction)
        memorize(roleAdvice)
    }

    override fun onReceive(event: GameEvent) {
        _myMemories.add(event)
    }

    override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
        val instruction = statementFormat.buildInstruction(context, claimableRoles, reportEligibility)
        repeat(2) {
            val completion = prompt(instruction)
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
                    intentForRecall = parsed.intent,
                    intentForChronicle = withMetadata(parsed.intent, completion.metadata),
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
            val completion = prompt(instruction)
            try {
                val (target, intent) = parseChoiceResponse(completion.text, candidates)
                val choice = Choice(
                    this, context, target,
                    intentForRecall = intent,
                    intentForChronicle = withMetadata(intent, completion.metadata),
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

    override fun watchEpilogue(chronicles: List<ChronicleView>) = Unit

    private fun prompt(instruction: String): Completion {
        val history = _myMemories.map { it.toRecallView().toHistoryEntry() }
        val instructionText = buildString {
            appendLine("【指示】")
            append(instruction)
        }
        @Suppress("TooGenericExceptionCaught")
        return try {
            languageModel.ask(gameDescription, history, instructionText)
        } catch (e: Exception) {
            GameOverSignal.throwAborted(e)
        }
    }

    private fun withMetadata(intent: String, metadata: ModelMetadata) =
        "$intent | ${metadata.toDisplayString()}"

    private val gameDescription = """
あなたはプレイヤー${name}です。あなたの役職はゲームの流れの「役職通知」をご確認ください。
ゲームの流れの冒頭にある指示に従って行動してください。
役職構成：人狼2・狂人1・村人3・占い師1・霊能者1・狩人1（計9人）
人狼は仲間の人狼を知っていますが狂人が誰かは知りません。狂人も人狼が誰かを知りません。また、ほかの役職も他人の役職は知りません。

【勝利条件】
村人（村人・占い師・霊能者・狩人）：人狼が全員死亡すれば勝利
狂人：人狼が勝利したとき自分も勝利。ただし人数上は村人側として数えられる
人狼：生存する人狼の数が村人側（狂人含む）の数以上になれば勝利

【夜のルール】
・人狼は毎晩3往復の密談ができます。
　初日は襲撃ができません。二日目以降は人狼以外のプレイヤーを一人選んで襲撃できます。襲撃先が人狼間で割れた場合は襲撃先候補からランダムに一人が襲撃されます。
・占い：毎晩一人のプレイヤーが人狼（黒）か、人狼以外（白。狂人も白扱いとなることに注意）かを知ることができます。
　初日は人狼以外のプレイヤーからランダムに1人が占われます。よって必ず結果は白となります。
　2日目以降は占い師が対象を選びます。
・霊能：前日に処刑されたプレイヤーが人狼（黒）か人狼以外（白。狂人も白扱いとなることに注意）かを知ることができます。
　初日は「前日の処刑」がないので霊能結果は得られません。
・狩人は自分以外のプレイヤーを一人選んで護衛できます。護衛先と襲撃先が一致した場合襲撃はなくなります（護衛成功）。
　初日は襲撃がないため護衛先の選択もしません。

【昼のルール】
・議論：開始時にランダムに決めた順番で全員が3回ずつ発言します
・発言者名は別途表示されるため、発言内で自分の名前を名乗る必要はありません
・投票：投票先は他のプレイヤーに開示されません。最多票のプレイヤーが処刑されます

【ローカルルール：対抗COの期限】
・ある役職を騙るCO（役職者を騙る発言）が出た場合、その役職の真の持ち主は議論一巡以内に対抗COしなければなりません。
・議論一巡以内に対抗COが出なければ、先に出たCOを真として扱って構いません。
・誰が真の役職者かを論理的に判断しやすくするため、このルールを設けています。

【ローカルルール：積極的な発言の推奨】
・この村では積極的・論理的であることが正義です。誰かを疑う言葉は議論を盛り上げるため歓迎されます。
・「攻撃的なのは人狼よりだ」などといった偏見は捨ててください。
    """.trimIndent()
}

// Uses <> to avoid collision with AI response delimiters (: and [])
private fun RecallView.toHistoryEntry(): String = when (this) {
    is RecallView.Observation -> "<$category> $content"
    is RecallView.AttributedObservation -> "<$category> $actor: $content"
    is RecallView.SelfAction -> "<$category> $content <$intent>"
}

