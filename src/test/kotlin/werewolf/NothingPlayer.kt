package werewolf

import werewolf.game.*

open class NothingPlayer(role: Role, override val name: String) : Player(role) {
    override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim = error("speak not expected")
    override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility): SpeakingUrge = error("feelUrge not expected")
    override fun onReceive(event: GameEvent) { error("onReceive not expected") }
    override fun choose(context: SelectionContext): Choice = error("choose not expected")
    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) { error("watchEpilogue not expected") }
}
