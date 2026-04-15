package org.example

class HumanPlayer(override val role: Role, override val name: String, private val io: PlayerIO) : Player {
    override fun notifyRole() {
        io.sendMessage(name, "役職通知", "あなたの役職は「${role.displayName}」です。")
    }

    override fun nightAction(players: List<Player>, isFirstNight: Boolean): NightAction =
        role.nightAction(this, players, io, isFirstNight)

    override fun vote(players: List<Player>): Player {
        val candidates = players.filterNot { it === this }
        return io.prompt(name, "投票", "投票先を選んでください", candidates)
    }

    override fun onGameOver(winnerSide: Side) {
        val result = if (role.side == winnerSide) "勝利" else "敗北"
        io.sendMessage(name, "ゲーム終了", "${winnerSide.displayName}陣営の勝利です！あなたは${result}しました。")
    }

    override fun onPlayerExecuted(player: Player) {
        if (player === this) {
            io.sendMessage(name, "処刑通知", "あなたは処刑されました。")
        } else {
            io.sendMessage(name, "処刑通知", "${player.name} が処刑されました。")
        }
    }

    override fun onDivineResult(target: Player, result: DivineResult) {
        io.sendMessage(name, "占い結果", "${target.name} は「${result.displayName}」です。")
    }

    override fun onMediumReveal(target: Player, result: MediumResult) {
        io.sendMessage(name, "霊視結果", "${target.name} は「${result.displayName}」です。")
    }

    override fun discuss(players: List<Player>): String =
        io.promptText(name, "議論", "発言してください")

    override fun onDiscussionRound(round: Int, statements: List<Pair<String, String>>) {
        val content = statements.joinToString("\n") { (speaker, statement) -> "$speaker: $statement" }
        io.sendMessage(name, "議論（${round}ラウンド目）結果", content)
    }

    override fun onPlayerAttacked(player: Player) {
        if (player === this) {
            io.sendMessage(name, "襲撃通知", "あなたは襲撃されました。")
        } else {
            io.sendMessage(name, "襲撃通知", "${player.name} が襲撃されました。")
        }
    }
}