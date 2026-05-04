package org.example

fun fakeCitizenWinSignal(): GameOverSignal = try {
    GameOverSignal.throwIfGameOver(AliveCounts(mapOf(Side.CITIZEN to 2, Side.WEREWOLF to 0)))
    error("unreachable")
} catch (s: GameOverSignal) { s }
