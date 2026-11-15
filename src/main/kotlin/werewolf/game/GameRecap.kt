package werewolf.game

class GameRecap(private val playerManager: PlayerManager, private val signal: GameOverSignal) {
    fun chronicles(): List<ChronicleView> =
        events()
            .filterNot { isRedundantWithClaim(it) }
            .map { it.toChronicleView() }

    // 行動の付随情報を判断方式ごとにまとめ、全体の合計と、プレイヤー別の合計を表示する
    fun metadataTotals(): List<String> {
        val byMethod = allPlayerMetadata().groupBy { it.metadata.decisionMethod }
        return listOf("【判断方式別合計】") +
            byMethod.map { (method, entries) -> "$method ${countAndBody(sum(entries))}" } +
            "【判断方式・プレイヤー別合計】" +
            byMethod.flatMap { (method, entries) -> listOf(method) + playerLines(entries) }
    }

    private fun playerLines(entries: List<PlayerMetadata>): List<String> =
        entries.groupBy { it.playerName }.map { (name, own) -> "  [$name] ${countAndBody(sum(own))}" }

    private fun allPlayerMetadata(): List<PlayerMetadata> =
        playerManager.allPlayers.flatMap { player -> summablesOf(player).map { PlayerMetadata(player.name, it) } }

    private fun summablesOf(player: Player): List<SummableMetadata> =
        player.reveal(signal)
            .distinct()
            .filterIsInstance<PlayerAction>()
            .map { it.metadata.toSummable() }

    private fun sum(entries: List<PlayerMetadata>): SummableMetadata =
        entries.map { it.metadata }.reduce(SummableMetadata::plus)

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

    // 誰の行動かと、その付随情報
    private class PlayerMetadata(val playerName: String, val metadata: SummableMetadata)
}
