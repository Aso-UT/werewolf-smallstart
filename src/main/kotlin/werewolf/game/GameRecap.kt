package werewolf.game

class GameRecap(private val playerManager: PlayerManager, private val signal: GameOverSignal) {
    fun chronicles(): List<ChronicleView> =
        events()
            .filterNot { isRedundantWithClaim(it) }
            .map { it.toChronicleView() }

    // 行動の付随情報を、判断方式別と、判断方式・プレイヤー別に合算した表示行
    fun metadataTotals(): List<String> {
        val byPlayer = playerManager.allPlayers.associate { it.name to sumByDecisionMethod(summablesOf(it)) }
        val totals = sumByDecisionMethod(byPlayer.values.flatMap { it.values })
        return listOf("【判断方式別合計】") +
            totals.map { (decisionMethod, total) -> "$decisionMethod ${countAndBody(total)}" } +
            "【判断方式・プレイヤー別合計】" +
            totals.keys.flatMap { listOf(it) + playerLines(byPlayer, it) }
    }

    private fun playerLines(byPlayer: Map<String, Map<String, SummableMetadata>>, decisionMethod: String): List<String> =
        byPlayer.mapNotNull { (name, totals) -> totals[decisionMethod]?.let { "  [$name] ${countAndBody(it)}" } }

    private fun summablesOf(player: Player): List<SummableMetadata> =
        player.reveal(signal)
            .distinct()
            .filterIsInstance<PlayerAction>()
            .map { it.metadata.toSummable() }

    private fun sumByDecisionMethod(summables: List<SummableMetadata>): Map<String, SummableMetadata> =
        summables.groupBy { it.decisionMethod }.mapValues { (_, sameMethod) -> sameMethod.reduce(SummableMetadata::plus) }

    private fun countAndBody(total: SummableMetadata): String {
        val body = total.toDisplayString()
        return if (body.isEmpty()) "${total.count}件" else "${total.count}件: $body"
    }

    private fun events(): List<Recallable> =
        playerManager.allPlayers
            .flatMap { it.reveal(signal) }
            .distinct()
            .sortedBy { it.occurredAt }

    private fun isRedundantWithClaim(recallable: Recallable): Boolean = when (recallable) {
        is GameEvent.StatementMade -> !recallable.turn.isSimultaneous
        is GameEvent.WerewolfStatementMade -> !recallable.turn.isSimultaneous
        else -> false
    }
}
