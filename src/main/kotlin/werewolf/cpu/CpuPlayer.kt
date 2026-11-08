package werewolf.cpu

import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SpeakingUrge

abstract class CpuPlayer(role: Role) : Player(role) {
    override fun feelUrge(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility) =
        SpeakingUrge(this, context, Eagerness.DEFAULT, "")
    override fun watchEpilogue(chronicles: List<ChronicleView>) = Unit
}
