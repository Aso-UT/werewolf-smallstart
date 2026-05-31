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

        val blocks = manager.buildBlocks(listOf("A", "B"))

        assertEquals(1, blocks.size)
        assertEquals("【ここまでのゲームの流れ】\nA\nB", blocks.single().first)
        assertTrue(blocks.single().second)
    }

    @Test
    fun `commit advances committedItemCount`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B"))
        assertEquals(0, manager.committedItemCount)

        manager.commit()

        assertEquals(2, manager.committedItemCount)
    }

    @Test
    fun `second call preserves first block text exactly for cache key stability`() {
        val manager = HistoryBlockManager()
        val firstBlocks = manager.buildBlocks(listOf("A", "B"))
        manager.commit()

        val secondBlocks = manager.buildBlocks(listOf("A", "B", "C", "D"))

        assertEquals(firstBlocks[0].first, secondBlocks[0].first)
    }

    @Test
    fun `second block has no header`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B"))
        manager.commit()

        val blocks = manager.buildBlocks(listOf("A", "B", "C", "D"))

        assertEquals("C\nD", blocks[1].first)
    }

    @Test
    fun `all blocks get cache control when count is 4 or fewer`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F"))
        manager.commit()

        val blocks = manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H"))

        assertEquals(4, blocks.size)
        assertTrue(blocks.all { it.second })
    }

    @Test
    fun `oldest block loses cache control when count exceeds 4`() {
        val manager = HistoryBlockManager()
        manager.buildBlocks(listOf("A", "B"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F"))
        manager.commit()
        manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H"))
        manager.commit()

        val blocks = manager.buildBlocks(listOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"))

        assertEquals(5, blocks.size)
        assertFalse(blocks[0].second, "oldest block should have no cache control")
        assertTrue(blocks.drop(1).all { it.second }, "latest 4 blocks should have cache control")
    }

    @Test
    fun `retry with same history after parse failure returns identical blocks without extra block`() {
        val manager = HistoryBlockManager()
        // ask()成功・commit()済み → parse失敗 → _myMemoriesは変わらず同じhistoryでリトライ
        val firstBlocks = manager.buildBlocks(listOf("A", "B"))
        manager.commit()

        val retryBlocks = manager.buildBlocks(listOf("A", "B"))

        // 空のブロックが増えてcache_controlの枠を浪費しないことを確認
        assertEquals(firstBlocks, retryBlocks)
    }
}
