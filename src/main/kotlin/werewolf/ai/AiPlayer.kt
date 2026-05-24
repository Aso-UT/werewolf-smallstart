package werewolf.ai

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.DiscussionContext
import werewolf.game.FallbackChoice
import werewolf.game.FallbackClaim
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.Player
import werewolf.game.Recallable
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.Statement

class AiPlayer(
    role: Role,
    override val name: String,
    private val languageModel: LanguageModel,
    vararg instructions: Instruction,
) : Player(role) {
    private val _myMemories = mutableListOf<Recallable>()

    init {
        instructions.forEach {
            memorize(it)
            _myMemories.add(it)
        }
    }

    override fun onReceive(event: GameEvent) {
        _myMemories.add(event)
    }

    override fun speak(context: DiscussionContext): Claim {
        val instruction = """
            【${context.title}】${context.description}

            以下の形式で発言してください。
            ゲーム上の発言（100文字以内）[発言の真意（100文字以内）]
            例：占い師です。Aliceは白でした。[狂人として占い師を偽装し、信用を得るための発言]
        """.trimIndent()
        repeat(2) {
            val completion = prompt(instruction)
            try {
                val (text, intent) = parseSpeakResponse(completion.text)
                val claim = Claim(
                    this, context, Statement.Plain(text),
                    intentForRecall = intent,
                    intentForChronicle = withMetadata(intent, completion.metadata),
                )
                _myMemories.add(claim)
                return claim
            } catch (_: InvalidAiInputException) {
                // AI応答が不正な形式のため、記録して次のイテレーションでリトライする
                memorize(InvalidAiInput(completion.text, completion.metadata))
            }
        }
        return FallbackClaim(this, context)
    }

    private fun parseSpeakResponse(input: String): Pair<String, String> {
        val separatorIdx = input.indexOf("[").takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「発言[真意]」の形式ではありません: $input")
        val text = input.substring(0, separatorIdx).trim()
        val intent = input.substring(separatorIdx + 1).removeSuffix("]").trim()
        return text to intent
    }

    override fun choose(context: SelectionContext): Choice {
        val candidates = context.candidates()
        val instruction = """
            ${context.title}：${context.description}

            候補：${candidates.joinToString("、") { it.name }}

            「候補名：選んだ理由（200文字以内）」の形式で答えてください。
            例：${candidates.first().name}：最も怪しいと思うため
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
                memorize(InvalidAiInput(completion.text, completion.metadata))
            }
        }
        return FallbackChoice(this, context)
    }

    private fun parseChoiceResponse(input: String, candidates: List<Player>): Pair<Player, String> {
        val (targetString, intent) = input.split("：", ":", limit = 2).takeIf { it.size == 2 }
            ?: throw InvalidAiInputException("「ターゲット：理由」の形式ではありません: $input")
        val target = candidates.firstOrNull { it.name == targetString.trim() }
            ?: throw InvalidAiInputException("候補に存在しないターゲットです: ${targetString.trim()}")
        return target to intent.trim()
    }

    private class InvalidAiInputException(message: String) : Exception(message)

    override fun watchEpilogue(chronicles: List<Recallable>) = Unit

    private fun prompt(instruction: String): Completion {
        val user = buildString {
            appendLine("【指示】")
            appendLine(instruction)
            appendLine()
            appendLine("【ここまでのゲームの流れ】")
            if (_myMemories.isEmpty()) appendLine("（なし）")
            else _myMemories.forEach { appendLine(it.recall()) }
        }
        @Suppress("TooGenericExceptionCaught")
        return try {
            languageModel.ask(gameDescription, user)
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

【勝利条件】
村人（村人・占い師・霊能者・狩人）：人狼が全員死亡すれば勝利
狂人：人狼が勝利したとき自分も勝利。ただし人数上は村人側として数えられる
人狼：生存する人狼の数が村人側（狂人含む）の数以上になれば勝利

【夜のルール】
・人狼は毎晩3往復の密談ができます。
　二日目以降は人狼以外のプレイヤーを一人選んで襲撃できます。襲撃先が人狼間で割れた場合は襲撃先候補からランダムに一人が襲撃されます。
・占い：毎晩一人のプレイヤーが人狼（黒）か、人狼以外（白。狂人も白扱いとなることに注意）かを知ることができます。
　初日は村人陣営からランダムに1人が占われます。よって必ず結果は白となります。
　2日目以降は占い師が対象を選びます。
・霊能：前日に処刑されたプレイヤーが人狼（黒）か人狼以外（白。狂人も白扱いとなることに注意）かを知ることができます。
　初日は「前日の処刑」がないので霊能結果は得られません。
・狩人は自分以外のプレイヤーを一人選んで護衛できます。護衛先と襲撃先が一致した場合襲撃はなくなります（護衛成功）。
　初日は襲撃がないため護衛先の選択もしません。

【昼のルール】
・議論：開始時にランダムに決めた順番で全員が3回ずつ発言します
・発言者名は別途表示されるため、発言内で自分の名前を名乗る必要はありません
・投票：投票先は他のプレイヤーに開示されません。最多票のプレイヤーが処刑されます
    """.trimIndent()
}
