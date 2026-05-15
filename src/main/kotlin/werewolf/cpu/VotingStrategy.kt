package werewolf.cpu

import werewolf.game.Player

fun interface VotingStrategy {
    fun selectVoteTarget(candidates: List<Player>): Player
}
