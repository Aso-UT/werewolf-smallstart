package werewolf.cpu

import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SpeakingUrge
import werewolf.game.UnimplementedActionMetadata

abstract class CpuPlayer(role: Role) : Player(role) {
    override fun feelUrge(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility) =
        SpeakingUrge(this, context, Eagerness.DEFAULT, "", UnimplementedActionMetadata)
    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) = Unit
}
