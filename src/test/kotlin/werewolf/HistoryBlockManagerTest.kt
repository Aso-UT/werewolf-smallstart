package werewolf

import werewolf.ai.anthropic.HistoryBlockManager
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HistoryBlockManagerTest {

    @Test
    fun `first call returns one block with header and cache control`() {
        val manager = HistoryBlockManager()

        val blocks = manager.buildBlocks(listOf("A", "B", "C"))

        assertEquals(1, blocks.size)
        assertEquals("【ここまでのゲームの流れ】\nA\nB\nC", blocks.single().first)
        assertTrue(blocks.single().second)
    }

    @Test
    fun `commit advances committedItemCount`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B", "C"))
        assertEquals(0, manager.committedItemCount)

        manager.commit()

        assertEquals(3, manager.committedItemCount)
    }

    @Test
    fun `second call preserves first block text exactly for cache key stability`() {
        val manager = HistoryBlockManager()
        val firstBlocks = manager.buildBlocks(listOf("A", "B", "C"))
        manager.commit()

        val secondBlocks = manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F"))

        assertEquals(firstBlocks[0].first, secondBlocks[0].first)
    }

    @Test
    fun `second block has no header`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B", "C"))
        manager.commit()

        val blocks = manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F"))

        assertEquals("D\nE\nF", blocks[1].first)
    }

    @Test
    fun `all blocks get cache control when count is 4 or fewer`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B", "C"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H", "I"))
        manager.commit()

        val blocks = manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"))

        assertEquals(4, blocks.size)
        assertTrue(blocks.all { it.second })
    }

    @Test
    fun `oldest block loses cache control when count exceeds 4`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B", "C"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H", "I"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L"))
        manager.commit()

        val blocks = manager.buildBlocks(
            listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O")
        )

        assertEquals(5, blocks.size)
        assertFalse(blocks[0].second, "oldest block should have no cache control")
        assertTrue(blocks.drop(1).all { it.second }, "latest 4 blocks should have cache control")
    }

    @Test
    fun `uncommitted items are re-batched with new items after API failure`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B", "C"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F")) // API失敗 → commit()せず

        val blocks = manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H", "I"))

        // 失敗分(D,E,F)と新規(G,H,I)が1ブロックに結合される
        assertEquals(2, blocks.size)
        assertEquals("D\nE\nF\nG\nH\nI", blocks[1].first)
    }

    @Test
    fun `committed blocks are preserved after API failure`() {
        val manager = HistoryBlockManager()
        val firstBlocks = manager.buildBlocks(listOf("A", "B", "C"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F")) // API失敗 → commit()せず

        val blocks = manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H", "I"))

        assertEquals(firstBlocks[0].first, blocks[0].first)
    }
}
