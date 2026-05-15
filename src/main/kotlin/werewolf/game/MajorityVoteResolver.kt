package werewolf.game

object MajorityVoteResolver {
    fun <T> resolve(votes: List<T>): T? {
        if (votes.isEmpty()) return null
        return majority(votes)
    }

    fun <T> resolveNonEmpty(votes: List<T>): T {
        require(votes.isNotEmpty()) { "votes must not be empty" }
        return majority(votes)
    }

    private fun <T> majority(votes: List<T>): T {
        val countByCandidate = votes.groupingBy { it }.eachCount()
        val maxCount = countByCandidate.values.max()
        return countByCandidate.filter { it.value == maxCount }.keys.random()
    }
}
