package org.example

// Each subclass must have a private constructor and a companion send() function
// that creates and dispatches atomically. This ensures notification targets are
// always defined explicitly at the point of creation.
sealed class GameEvent {
    abstract val title: String
    abstract fun body(self: Player): String
    protected abstract val recipients: Notifiable
    protected fun dispatch() = recipients.receive(this)

    @ConsistentCopyVisibility
    data class RoleAssigned private constructor(val role: Role, private val recipient: Player) : GameEvent() {
        override val title = "役職通知"
        override fun body(self: Player) = "あなたの役職は「${role.displayName}」です。"
        override val recipients: Notifiable = recipient
        companion object {
            fun send(role: Role, recipient: Player) = RoleAssigned(role, recipient).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class PlayerExecuted private constructor(val executed: Player, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "処刑通知"
        override fun body(self: Player) =
            if (executed === self) "あなたは処刑されました。" else "${executed.name} が処刑されました。"
        override val recipients: Notifiable = allPlayers
        companion object {
            fun send(executed: Player, allPlayers: AllPlayers) = PlayerExecuted(executed, allPlayers).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class PlayerAttacked private constructor(val attacked: Player, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "襲撃通知"
        override fun body(self: Player) =
            if (attacked === self) "あなたは襲撃されました。" else "${attacked.name} が襲撃されました。"
        override val recipients: Notifiable = allPlayers
        companion object {
            fun send(attacked: Player, allPlayers: AllPlayers) = PlayerAttacked(attacked, allPlayers).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class Divined private constructor(val target: Player, val result: DivineResult, private val recipient: Player) : GameEvent() {
        override val title = "占い結果"
        override fun body(self: Player) = "${target.name} は「${result.displayName}」です。"
        override val recipients: Notifiable = recipient
        companion object {
            fun send(target: Player, result: DivineResult, recipient: Player) = Divined(target, result, recipient).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class MediumRevealed private constructor(val target: Player, val result: MediumResult, private val recipient: Player) : GameEvent() {
        override val title = "霊視結果"
        override fun body(self: Player) = "${target.name} は「${result.displayName}」です。"
        override val recipients: Notifiable = recipient
        companion object {
            fun send(target: Player, result: MediumResult, recipient: Player) = MediumRevealed(target, result, recipient).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class StatementMade private constructor(val round: Int, val speakerName: String, val statement: String, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "発言（${round}ラウンド目）"
        override fun body(self: Player) = "$speakerName: $statement"
        override val recipients: Notifiable = allPlayers
        companion object {
            fun send(round: Int, speakerName: String, statement: String, allPlayers: AllPlayers) =
                StatementMade(round, speakerName, statement, allPlayers).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class TimeChanged private constructor(val timeOfDay: TimeOfDay, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "${timeOfDay.name}の訪れ"
        override fun body(self: Player) = "${timeOfDay.displayName}になりました。"
        override val recipients: Notifiable = allPlayers
        companion object {
            fun send(timeOfDay: TimeOfDay, allPlayers: AllPlayers) = TimeChanged(timeOfDay, allPlayers).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class MorningReport private constructor(val victim: Player?, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "朝の報告"
        override fun body(self: Player) =
            if (victim != null) "昨夜は ${victim.name} が襲撃されました。" else "昨夜は犠牲者がいませんでした。"
        override val recipients: Notifiable = allPlayers
        companion object {
            fun send(victim: Player?, allPlayers: AllPlayers) = MorningReport(victim, allPlayers).dispatch()
        }
    }

    @ConsistentCopyVisibility
    data class GameOver private constructor(val winnerSide: Side, private val allPlayers: AllPlayers) : GameEvent() {
        override val title = "ゲーム終了"
        override fun body(self: Player): String {
            val result = if (self.role.side == winnerSide) "勝利" else "敗北"
            return "${winnerSide.displayName}陣営の勝利です！あなたは${result}しました。"
        }
        override val recipients: Notifiable = allPlayers
        companion object {
            fun send(winnerSide: Side, allPlayers: AllPlayers) = GameOver(winnerSide, allPlayers).dispatch()
        }
    }
}