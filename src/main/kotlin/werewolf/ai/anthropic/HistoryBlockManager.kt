package werewolf.ai.anthropic

class HistoryBlockManager {
    private val committedBlocks: MutableList<String> = mutableListOf()
    private var pendingBlock: String? = null
    private var pendingItemCount = 0
    var committedItemCount = 0
        private set

    fun buildBlocks(history: List<String>): List<Pair<String, Boolean>> {
        val newItems = history.drop(committedItemCount)
        pendingBlock = if (newItems.isEmpty()) null else buildString {
            if (committedItemCount == 0) appendLine("【ここまでのゲームの流れ】")
            newItems.forEach { appendLine(it) }
        }.trimEnd()
        pendingItemCount = history.size

        val allBlocks = committedBlocks + listOfNotNull(pendingBlock)
        val firstCachedIndex = maxOf(0, allBlocks.size - MAX_CACHED_BLOCKS)
        return allBlocks.mapIndexed { i, text -> text to (i >= firstCachedIndex) }
    }

    fun commit() {
        pendingBlock?.let {
            committedBlocks.add(it)
            committedItemCount = pendingItemCount
            pendingBlock = null
        }
    }

    companion object {
        private const val MAX_CACHED_BLOCKS = 4
    }
}
