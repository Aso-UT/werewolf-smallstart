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
sealed class UrgeButton(val label: String) {
    abstract fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata): SpeakingUrge

    protected val intent get() = "「${label}」を押した"

    class CounterClaim(private val role: Role) : UrgeButton("対抗CO（${role.displayName}）") {
        override fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata) =
            CounterClaimSpeakingUrge(player, context, role, intent, metadata)
    }

    data object Announcement : UrgeButton("CO・結果報告") {
        override fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata) =
            SpeakingUrge(player, context, Eagerness.ANNOUNCEMENT, intent, metadata)
    }

    data object Speak : UrgeButton("発言を希望") {
        override fun press(player: Player, context: DiscussionContext, metadata: ActionMetadata) =
            SpeakingUrge(player, context, Eagerness.URGENT, intent, metadata)
    }

    companion object {
        fun available(eligibility: UrgeEligibility): List<UrgeButton> = listOfNotNull(
            eligibility.counterClaimRole?.let { CounterClaim(it) },
            Announcement.takeIf { eligibility.canAnnounce },
            Speak,
        )
    }
}
