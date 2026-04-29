package org.example

interface VotingStrategy {
    fun selectVoteTarget(candidates: List<Player>): Player
}
