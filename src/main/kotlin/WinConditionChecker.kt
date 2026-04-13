package org.example

object WinConditionChecker {
    fun winningSide(aliveCounts: AliveCounts): Side? {
        // TODO: 複数陣営同時勝利時や、遅延勝利判定などの複雑なパターン
        return Side.entries.firstOrNull { it.hasWon(aliveCounts) }
    }
}