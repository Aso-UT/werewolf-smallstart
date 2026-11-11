package werewolf.game

class GameRecap(private val playerManager: PlayerManager, private val signal: GameOverSignal) {
    fun chronicles(): List<ChronicleView> =
        events()
            .filterNot { isRedundantWithClaim(it) }
            .map { it.toChronicleView() }

    // 行動の付随情報を、判断方式別と、判断方式・プレイヤー別に合算した表示行
    fun metadataTotals(): List<String> {
        val byPlayer = playerManager.allPlayers.associate { it.name to sumByDecisionMethod(summablesOf(it)) }
        val totals = sumByDecisionMethod(byPlayer.values.flatten())
        return listOf("【判断方式別合計】") +
            totals.map { "${it.decisionMethod} ${countAndBody(it)}" } +
            "【判断方式・プレイヤー別合計】" +
            totals.flatMap { listOf(it.decisionMethod) + playerLines(byPlayer, it.decisionMethod) }
    }

    private fun playerLines(byPlayer: Map<String, List<SummableMetadata>>, decisionMethod: String): List<String> =
        byPlayer.mapNotNull { (name, totals) ->
            totals.singleOrNull { it.decisionMethod == decisionMethod }?.let { "  [$name] ${countAndBody(it)}" }
        }

    private fun summablesOf(player: Player): List<SummableMetadata> =
        player.reveal(signal)
            .distinct()
            .filterIsInstance<PlayerAction>()
            .map { it.metadata.toSummable() }

    private fun sumByDecisionMethod(summables: List<SummableMetadata>): List<SummableMetadata> =
        summables.groupBy { it.decisionMethod }.values.map { it.reduce(SummableMetadata::plus) }

    private fun countAndBody(total: SummableMetadata): String {
        val body = total.toDisplayString()
        return if (body.isEmpty()) "${total.count}件" else "${total.count}件: $body"
    }

    private fun events(): List<Recallable> =
        playerManager.allPlayers
            .flatMap { it.reveal(signal) }
            .distinct()
            .sortedBy { it.sequenceId }

    private fun isRedundantWithClaim(recallable: Recallable): Boolean =
        recallable is GameEvent.StatementMade || recallable is GameEvent.WerewolfStatementMade
}
