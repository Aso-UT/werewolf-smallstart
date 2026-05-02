package org.example

fun interface VotingStrategy {
    fun selectVoteTarget(candidates: List<Player>): Player
}
