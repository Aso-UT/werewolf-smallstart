package werewolf.game

sealed class SpeakingUrge(
    val player: Player,
    val context: DiscussionContext,
    val eagerness: Eagerness,
    private val intent: String,
    override val metadata: ActionMetadata,
) : Recallable(), PlayerAction {
    private val title get() = "${context.title} - 発言欲求"
    protected abstract val content: String

    // この発言希望で発言権を得たときに選べる発言の種類と、COできる役職
    abstract val statementTypes: Set<StatementType>
    abstract fun narrowClaimableRoles(claimableRoles: Set<Role>): Set<Role>

    override fun toRecallView() = RecallView.SelfAction(title, content, intent)
    override fun toChronicleView() = ChronicleView.Action(player.name, title, content, intentWithMetadata(intent))

    companion object {
        operator fun invoke(
            player: Player,
            context: DiscussionContext,
            eagerness: Eagerness,
            intent: String,
            metadata: ActionMetadata,
        ): SpeakingUrge = NormalSpeakingUrge(player, context, eagerness, intent, metadata)
    }
}

private class NormalSpeakingUrge(
    player: Player,
    context: DiscussionContext,
    eagerness: Eagerness,
    intent: String,
    metadata: ActionMetadata,
) : SpeakingUrge(player, context, eagerness, intent, metadata) {
    init {
        require(eagerness != Eagerness.COUNTER_CLAIM) { "Use CounterClaimSpeakingUrge for counter claims" }
    }

    override val content get() = "意欲 ${eagerness.value}"
    override val statementTypes get() = eagerness.statementTypes
    override fun narrowClaimableRoles(claimableRoles: Set<Role>) = claimableRoles
}

// 対抗COで発言権を得たら、対抗する役職のCOか、その役職の結果報告しかできない
class CounterClaimSpeakingUrge(
    player: Player,
    context: DiscussionContext,
    val role: Role,
    intent: String,
    metadata: ActionMetadata,
) : SpeakingUrge(player, context, Eagerness.COUNTER_CLAIM, intent, metadata) {
    override val content get() = "意欲 ${eagerness.value}（${role.displayName}で対抗CO）"
    override val statementTypes get() =
        setOf(StatementType.ROLE_CLAIM) + StatementType.entries.filter { it.reporterRole == role }
    override fun narrowClaimableRoles(claimableRoles: Set<Role>) = claimableRoles intersect setOf(role)
}

class FallbackSpeakingUrge(player: Player, context: DiscussionContext) : SpeakingUrge(
    player, context, Eagerness.DEFAULT,
    intent = "回答取得に失敗したため既定の意欲を返却",
    metadata = FallbackActionMetadata,
) {
    override val content get() = "意欲 ${eagerness.value}"
    override val statementTypes get() = eagerness.statementTypes
    override fun narrowClaimableRoles(claimableRoles: Set<Role>) = claimableRoles
}
