package org.example

sealed class GameEvent {
    abstract val title: String
    abstract fun body(self: Player): String
    protected abstract val recipients: Notifiable
    fun dispatch() = recipients.receive(this)

    data class RoleAssigned(val role: Role, private val recipient: Player) : GameEvent() {
        override val title = "役職通知"
        override fun body(self: Player) = "あなたの役職は「${role.displayName}」です。"
        override val recipients: Notifiable = recipient
    }

    data class PlayerExecuted(val executed: Player, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "処刑通知"
        override fun body(self: Player) =
            if (executed === self) "あなたは処刑されました。" else "${executed.name} が処刑されました。"
        override val recipients: Notifiable = allPlayers
    }

    data class PlayerAttacked(val attacked: Player, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "襲撃通知"
        override fun body(self: Player) =
            if (attacked === self) "あなたは襲撃されました。" else "${attacked.name} が襲撃されました。"
        override val recipients: Notifiable = allPlayers
    }

    data class Divined(val target: Player, val result: DivineResult, private val recipient: Player) : GameEvent() {
        override val title = "占い結果"
        override fun body(self: Player) = "${target.name} は「${result.displayName}」です。"
        override val recipients: Notifiable = recipient
    }

    data class MediumRevealed(val target: Player, val result: MediumResult, private val recipient: Player) : GameEvent() {
        override val title = "霊視結果"
        override fun body(self: Player) = "${target.name} は「${result.displayName}」です。"
        override val recipients: Notifiable = recipient
    }

    data class StatementMade(val round: Int, val speakerName: String, val statement: String, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "発言（${round}ラウンド目）"
        override fun body(self: Player) = "$speakerName: $statement"
        override val recipients: Notifiable = allPlayers
    }

    data class TimeChanged(val timeOfDay: TimeOfDay, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "${timeOfDay.name}の訪れ"
        override fun body(self: Player) = "${timeOfDay.displayName}になりました。"
        override val recipients: Notifiable = allPlayers
    }

    data class MorningReport(val victim: Player?, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "朝の報告"
        override fun body(self: Player) =
            if (victim != null) "昨夜は ${victim.name} が襲撃されました。" else "昨夜は犠牲者がいませんでした。"
        override val recipients: Notifiable = allPlayers
    }

    data class GameOver(val winnerSide: Side, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "ゲーム終了"
        override fun body(self: Player): String {
            val result = if (self.role.side == winnerSide) "勝利" else "敗北"
            return "${winnerSide.displayName}陣営の勝利です！あなたは${result}しました。"
        }
        override val recipients: Notifiable = allPlayers
    }
}