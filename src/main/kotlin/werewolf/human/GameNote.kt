package werewolf.human

import werewolf.game.GameEvent
import werewolf.game.Statement
import werewolf.view.DivinationView
import werewolf.view.PlayerStatus
import werewolf.view.ReportEntry
import werewolf.view.SurvivalView

class GameNote(private val myName: String) {
    private val playerStatuses = mutableMapOf<String, PlayerStatus>()
    private val playerNames = mutableListOf<String>()
    private val myDivineResults = mutableMapOf<String, String>()
    private val myMediumResults = mutableMapOf<String, String>()
    private val claimedDivineResults = mutableMapOf<Pair<String, String>, String>()
    private val claimedMediumResults = mutableMapOf<Pair<String, String>, String>()

    fun post(event: GameEvent) {
        when (event) {
            is GameEvent.PlayersAnnounced -> event.players.forEach {
                playerStatuses[it.name] = PlayerStatus.ALIVE
                playerNames += it.name
            }
            is GameEvent.PlayerExecuted -> playerStatuses[event.executed.name] = PlayerStatus.EXECUTED
            is GameEvent.PlayerAttacked -> playerStatuses[event.attacked.name] = PlayerStatus.ATTACKED
            is GameEvent.Divined -> myDivineResults[event.target.name] = event.result.displayName
            is GameEvent.MediumRevealed -> myMediumResults[event.target.name] = event.result.displayName
            is GameEvent.StatementMade -> when (val stmt = event.statement) {
                is Statement.DivinationReport ->
                    claimedDivineResults[event.speakerName to stmt.target.name] = stmt.result.displayName
                is Statement.MediumReport ->
                    claimedMediumResults[event.speakerName to stmt.target.name] = stmt.result.displayName
                else -> Unit
            }
            else -> Unit
        }
    }

    fun summary(): SurvivalView = SurvivalView(playerStatuses.toMap())

    fun divinationSummary(): DivinationView {
        val divine = myDivineResults.map { (target, result) -> ReportEntry(myName, target, result, trusted = true) } +
            claimedDivineResults.map { (key, result) -> ReportEntry(key.first, key.second, result, trusted = false) }
        val medium = myMediumResults.map { (target, result) -> ReportEntry(myName, target, result, trusted = true) } +
            claimedMediumResults.map { (key, result) -> ReportEntry(key.first, key.second, result, trusted = false) }
        return DivinationView(playerNames.toList(), divine, medium)
    }
}
