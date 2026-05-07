package org.example

class PocAiPlayer(
    role: Role,
    override val name: String,
    private val languageModel: LanguageModel = ConsoleLanguageModel(),
) : Player(role) {
    private val eventLog = mutableListOf<GameEvent>()

    override fun onReceive(event: GameEvent) {
        eventLog.add(event)
    }

    override fun buildStatement(context: DiscussionContext): Statement {
        val instruction = """
            【${context.title}】${context.description}

            以下の形式で発言してください。
            ゲーム上の発言（100文字以内）[発言の真意（100文字以内）]
            例：占い師です。Aliceは白でした。[狂人として占い師を偽装し、信用を得るための発言]
        """.trimIndent()
        repeat(2) {
            val input = prompt(instruction) ?: ""
            val separatorIdx = input.indexOf("[").takeIf { it >= 0 }
            if (separatorIdx != null) return Statement.Plain(input.substring(0, separatorIdx).trim())
        }
        // 2回失敗したため空文字を返す
        return Statement.Plain("")
    }

    override fun selectTarget(context: SelectionContext): Player {
        val candidates = context.candidates()
        val candidateNames = candidates.joinToString("、") { it.name }
        val instruction = """
            ${context.title}：${context.description}

            候補：$candidateNames

            「候補名：選んだ理由（200文字以内）」の形式で答えてください。
            例：${candidates.first().name}：最も怪しいと思うため
        """.trimIndent()
        repeat(2) {
            val input = prompt(instruction) ?: ""
            val playerName = input.split("：", ":").first().trim()
            val target = candidates.firstOrNull { it.name == playerName }
            if (target != null) return target
        }
        // 2回失敗したためランダムで選択
        return candidates.random()
    }

    override fun watchEpilogue(events: List<GameEvent>) {
        val instruction = buildString {
            appendLine("=== エピローグ（プレイヤー $name） ===")
            events.forEach { appendLine("[${it.recipientName}] [${it.title}] ${it.body()}") }
            append("プレイヤーとして200文字以内でゲームの振り返りをしてください。")
        }
        prompt(instruction)
    }

    private fun prompt(instruction: String): String? {
        val fullPrompt = buildString {
            appendLine()
            appendLine(SEPARATOR)
            appendLine("AIプレイヤー $name へのプロンプト")
            appendLine(SEPARATOR)
            appendLine("【指示】")
            appendLine(instruction)
            appendLine()
            appendLine("【ゲームの説明】")
            appendLine(gameDescription)
            appendLine()
            appendLine("【ここまでのゲームの流れ】")
            if (eventLog.isEmpty()) appendLine("（なし）")
            else eventLog.forEach { appendLine("[${it.title}] ${it.body()}") }
            append(SEPARATOR)
        }
        return languageModel.ask(fullPrompt)
    }

    companion object {
        private const val SEPARATOR_WIDTH = 50
        private val SEPARATOR = "=".repeat(SEPARATOR_WIDTH)
    }

    private val gameDescription = """
あなたはプレイヤー${name}です。あなたの役職はゲームの流れの「役職通知」をご確認ください。
役職構成：人狼2・狂人1・村人3・占い師1・霊能者1・狩人1（計9人）

【勝利条件】
村人（村人・占い師・霊能者・狩人）：人狼が全員死亡すれば勝利
狂人：人狼が勝利したとき自分も勝利。ただし人数上は村人側として数えられる
人狼：生存する人狼の数が村人側（狂人含む）の数以上になれば勝利

【夜のルール】
・人狼は毎晩3往復の密談ができます（初日は襲撃しません）
・占い：初日は村人陣営からランダムに1人が占われます。2日目以降は占い師が対象を選びます
・狩人は初日夜は護衛しません

【昼のルール】
・議論：開始時にランダムに決めた順番で全員が3回ずつ発言します
・発言者名は別途表示されるため、発言内で自分の名前を名乗る必要はありません
・投票：投票先は他のプレイヤーに開示されません。最多票のプレイヤーが処刑されます
    """.trimIndent()
}
