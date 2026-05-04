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
        events.forEach { println("[${it.title}] ${it.body()}") }
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
プレイヤーは7人で、1~7の番号が付けられており、あなたは${name}です。
このゲームでは、プレイヤーは人狼・村人などの役職を与えられ、役職ごとの勝利条件を満たすために行動します。
大きな対立は「夜に村人を襲撃する人狼」と「昼の議論で人狼の疑いがあるものを絞り、投票で処刑して対抗する村人」です。
人狼は「村人陣営と人狼陣営の人数が同数になれば勝利」です。
村人陣営は「人狼陣営が全員死亡すれば勝利」です。

村人陣営には村人のほかに以下の役職があります。
「選択した他者一人の役職を毎晩知れる占い師」
「前日に処刑したプレイヤーの役職を知れる霊能者」
「毎晩他者一人を選んで護衛できる狩人（人狼の襲撃相手と護衛相手が一致した場合、襲撃が失敗します。）」
人狼陣営は人狼だけですが、「人狼に味方し、人狼陣営の勝利が自らの勝利となる狂人」という役職があります。

初期の役職配分は人狼2人・狂人1人・村人1人・占い師1人・霊能者1人・狩人1人です。
    """.trimIndent()
}
