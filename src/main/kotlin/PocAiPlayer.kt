package org.example

class PocAiPlayer(role: Role, override val name: String) : Player(role) {
    private val eventLog = mutableListOf<GameEvent>()

    override fun onReceive(event: GameEvent) {
        eventLog.add(event)
    }

    override fun discuss(players: List<Player>): Statement {
        printPrompt("50文字以内で発言してください")
        return Statement.Plain(readLine()?.trim() ?: "")
    }

    override fun selectTarget(context: SelectionContext): Player {
        val candidates = context.candidates()
        val candidateNames = candidates.joinToString(", ") { it.name }
        printPrompt("${context.title}：${context.description}（候補: $candidateNames）")
        while (true) {
            val input = readLine()?.trim() ?: ""
            val target = candidates.firstOrNull { it.name == input }
            if (target != null) return target
            println("「$input」は候補にいません。再入力してください。")
        }
    }

    override fun watchEpilogue(events: List<GameEvent>) {
        println()
        println("=== エピローグ（プレイヤー $name） ===")
        events.forEach { println("[${it.recipientName}] [${it.title}] ${it.body()}") }
        printPrompt("プレイヤーとして200文字以内でゲームの振り返りをしてください。")
        readLine()
    }

    private fun printPrompt(instruction: String) {
        println()
        println(SEPARATOR)
        println("AIプレイヤー $name へのプロンプト")
        println(SEPARATOR)
        println("【指示】")
        println(instruction)
        println()
        println("【ゲームの説明】")
        println(gameDescription)
        println()
        println("【ここまでのゲームの流れ】")
        if (eventLog.isEmpty()) println("（なし）")
        else eventLog.forEach { println("[${it.title}] ${it.body()}") }
        println(SEPARATOR)
        print("回答 > ")
    }

    companion object {
        private const val SEPARATOR_WIDTH = 50
        private val SEPARATOR = "=".repeat(SEPARATOR_WIDTH)
    }

    private val gameDescription = """
あなたはプレイヤー${name}です。あなたの役職はゲームの流れの「役職通知」をご確認ください。
役職構成：人狼2・狂人1・村人1・占い師1・霊能者1・狩人1（計7人）

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
