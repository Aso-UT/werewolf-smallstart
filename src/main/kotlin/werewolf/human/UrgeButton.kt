package werewolf.human

import werewolf.game.ActionMetadata
import werewolf.game.CounterClaimSpeakingUrge
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Player
import werewolf.game.Role
import werewolf.game.SpeakingUrge
import werewolf.game.UrgeEligibility

// 人間が発言を希望するときに押すボタン。AIの発言意欲の段階に揃え、押したボタンで意欲が決まる
sealed class UrgeButton(val label: String, val isHidden: Boolean) {
    abstract fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata): SpeakingUrge

    protected val intent get() = "「${label}」を押した"

    class CounterClaim(private val role: Role) : UrgeButton("対抗CO（${role.displayName}）", isHidden = false) {
        override fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata) =
            CounterClaimSpeakingUrge(player, context, role, intent, metadata)
    }

    data object Announcement : UrgeButton("CO・結果報告", isHidden = false) {
        override fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata) =
            SpeakingUrge(player, context, Eagerness.ANNOUNCEMENT, intent, metadata)
    }

    data object Speak : UrgeButton("発言を希望", isHidden = false) {
        override fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata) =
            SpeakingUrge(player, context, Eagerness.URGENT, intent, metadata)
    }

    // 時間内にどのボタンも押さなかったときに押したことにする
    data object Pass : UrgeButton("発言を希望しない", isHidden = true) {
        override fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata) =
            SpeakingUrge(player, context, Eagerness.UNWILLING, "時間内にボタンを押さなかった", metadata)
    }

    companion object {
        fun available(eligibility: UrgeEligibility): List<UrgeButton> = listOfNotNull(
            eligibility.counterClaimRole?.let { CounterClaim(it) },
            Announcement.takeIf { eligibility.canAnnounce },
            Speak,
            Pass,
        )
    }
}

fun HumanPrompt.pressedUrgeButton(title: String, seconds: Int, buttons: List<UrgeButton>): UrgeButton {
    val (hidden, shown) = buttons.partition { it.isHidden }
    val label = promptSpeakingUrge(title, seconds, shown.map { it.label }, hidden.single().label)
    return buttons.single { it.label == label }
}
