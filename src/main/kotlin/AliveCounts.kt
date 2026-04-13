package org.example

class AliveCounts(alivePlayers: List<Player>) {
    private val counts: Map<Side, Int> = Side.entries.associateWith { side ->
        alivePlayers.count { it.role.side == side }
    }

    // Side.entriesを使って全陣営のカウントを設定するため、必ずgetできる
    operator fun get(side: Side): Int = counts.getValue(side)
}