package werewolf.cpu

import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.GameOverSignal
import werewolf.game.Player
import werewolf.game.UrgeEligibility
import werewolf.game.Role
import werewolf.game.SpeakingUrge
import werewolf.game.UnimplementedActionMetadata

abstract class CpuPlayer(role: Role) : Player(role) {
    override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility) =
        SpeakingUrge(this, context, Eagerness.DEFAULT, "", UnimplementedActionMetadata)
    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) = Unit
    override fun onRevealing(signal: GameOverSignal) { /* CPU players record everything as they act */ }
}
