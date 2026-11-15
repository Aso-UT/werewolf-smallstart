package werewolf.phase

import kotlinx.coroutines.CoroutineDispatcher
import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Player
import werewolf.game.SpeakingRight
import werewolf.game.SpeakingUrge

// 発言できる人たちの発言意欲を聞いて、次に誰が発言権を得るかを決める
class SpeakingRightGrant(private val dispatcher: CoroutineDispatcher) {
    fun grant(contexts: Map<Player, DiscussionContext>): List<SpeakingRight> {
        contexts.keys.singleOrNull()?.let { return listOf(SpeakingRight.Uncontested(it)) }
        val winners = mostEagerUrges(collectUrges(contexts))
        val speaking = if (speaksSimultaneously(winners)) winners else listOf(winners.random())
        return speaking.map { SpeakingRight.Won(it) }
    }

    // COや結果報告は、後から出した方が前の人の発言に合わせられて有利になるため、同じ段階の全員に同時に出させる。
    // 意欲が同じ人だけをまとめるので、対抗COと告知が一つの同時発言に混ざることはない
    private fun speaksSimultaneously(winners: List<SpeakingUrge>): Boolean =
        winners.first().eagerness >= Eagerness.ANNOUNCEMENT

    private fun mostEagerUrges(urges: List<SpeakingUrge>): List<SpeakingUrge> {
        val highest = urges.maxOf { it.eagerness }
        return urges.filter { it.eagerness == highest }
    }

    private fun collectUrges(contexts: Map<Player, DiscussionContext>): List<SpeakingUrge> =
        contexts.keys.askInParallel(dispatcher) { it.speakingUrge(contexts.getValue(it)) }.values.toList()
}
