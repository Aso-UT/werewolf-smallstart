package org.example

sealed class GameEvent {
    abstract val title: String
    abstract fun body(self: Player): String

    data class PlayerExecuted(val executed: Player) : GameEvent() {
        override val title = "処刑通知"
        override fun body(self: Player) =
            if (executed === self) "あなたは処刑されました。" else "${executed.name} が処刑されました。"
    }

    data class PlayerAttacked(val attacked: Player) : GameEvent() {
        override val title = "襲撃通知"
        override fun body(self: Player) =
            if (attacked === self) "あなたは襲撃されました。" else "${attacked.name} が襲撃されました。"
    }

    data class Divined(val target: Player, val result: DivineResult) : GameEvent() {
        override val title = "占い結果"
        override fun body(self: Player) = "${target.name} は「${result.displayName}」です。"
    }

    data class MediumRevealed(val target: Player, val result: MediumResult) : GameEvent() {
        override val title = "霊視結果"
        override fun body(self: Player) = "${target.name} は「${result.displayName}」です。"
    }

    data class DiscussionRound(val round: Int, val statements: List<Pair<String, String>>) : GameEvent() {
        override val title = "議論（${round}ラウンド目）結果"
        override fun body(self: Player) =
            statements.joinToString("\n") { (speaker, statement) -> "$speaker: $statement" }
    }

    data class GameOver(val winnerSide: Side) : GameEvent() {
        override val title = "ゲーム終了"
        override fun body(self: Player): String {
            val result = if (self.role.side == winnerSide) "勝利" else "敗北"
            return "${winnerSide.displayName}陣営の勝利です！あなたは${result}しました。"
        }
    }
}
