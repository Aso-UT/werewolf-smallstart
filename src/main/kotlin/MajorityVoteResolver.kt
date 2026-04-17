package org.example

object MajorityVoteResolver {
    fun <T> resolve(votes: List<T>): T? {
        if (votes.isEmpty()) return null
        val countByCandidate = votes.groupingBy { it }.eachCount()
        val maxCount = countByCandidate.values.max()
        return countByCandidate.filter { it.value == maxCount }.keys.random()
    }
}
