package org.example

object WinConditionChecker {
    fun winningSide(aliveCounts: AliveCounts): Side? {
        return Side.entries.firstOrNull { it.hasWon(aliveCounts) }
    }
}