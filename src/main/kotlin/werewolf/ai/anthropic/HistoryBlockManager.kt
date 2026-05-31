package werewolf.ai.anthropic

class HistoryBlockManager {
    private val blocks: MutableList<String> = mutableListOf()
    var committedItemCount = 0
        private set

    fun buildBlocks(history: List<String>): List<Pair<String, Boolean>> {
        val newItems = history.drop(committedItemCount)
        if (newItems.isNotEmpty()) {
            blocks.add(buildString {
                if (committedItemCount == 0) appendLine("【ここまでのゲームの流れ】")
                newItems.forEach { appendLine(it) }
            }.trimEnd())
            committedItemCount = history.size
        }
        val firstCachedIndex = maxOf(0, blocks.size - MAX_CACHED_BLOCKS)
        return blocks.mapIndexed { i, text -> text to (i >= firstCachedIndex) }
    }

    companion object {
        private const val MAX_CACHED_BLOCKS = 4
    }
}
