package werewolf.game

// 議論で発言権を得た経緯。経緯に応じて、選べる発言を絞る
sealed class SpeakingRight {
    abstract val speaker: Player
    abstract fun narrow(context: DiscussionContext): DiscussionContext

    // 他に発言できる人がいなかったため、意欲を聞かずに発言権を得た
    class Uncontested(override val speaker: Player) : SpeakingRight() {
        override fun narrow(context: DiscussionContext) = context
    }

    // 最も高い意欲を示して発言権を得た
    class Won(private val urge: SpeakingUrge) : SpeakingRight() {
        override val speaker get() = urge.player
        override fun narrow(context: DiscussionContext) = context.narrowedTo(urge.statementTypes)
    }
}
