package org.example

class AliveCounts(private val counts: Map<Side, Int>) {
    // 全陣営のカウントが揃っていることを呼び出し側が保証するため、必ずgetできる
    operator fun get(side: Side): Int = counts.getValue(side)
}