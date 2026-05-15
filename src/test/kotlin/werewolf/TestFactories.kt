package werewolf

import werewolf.game.*

fun fakeCitizenWinSignal(): GameOverSignal = try {
    GameOverSignal.throwIfGameOver(AliveCounts(mapOf(Side.CITIZEN to 2, Side.WEREWOLF to 0)))
    error("unreachable")
} catch (s: GameOverSignal) { s }

fun openContext(
    players: List<Player> = emptyList(),
    round: Int = 1,
    day: Int = 1,
) = DiscussionContext.Open(round, day, players, players)
