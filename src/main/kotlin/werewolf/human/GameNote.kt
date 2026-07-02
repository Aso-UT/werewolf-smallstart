package werewolf.human

import werewolf.game.GameEvent
import werewolf.game.Statement
import werewolf.view.DivinationView
import werewolf.view.PlayerStatus
import werewolf.view.ReportEntry
import werewolf.view.SurvivalView

class GameNote {
    private val playerStatuses = mutableMapOf<String, PlayerStatus>()
    private val playerNames = mutableListOf<String>()
    private val divineResults = mutableMapOf<String, String>()
    private val mediumResults = mutableMapOf<String, String>()
    private val divineReports = mutableSetOf<ReportEntry>()
    private val mediumReports = mutableSetOf<ReportEntry>()

    fun post(event: GameEvent) {
        when (event) {
            is GameEvent.PlayersAnnounced -> event.players.forEach {
                playerStatuses[it.name] = PlayerStatus.ALIVE
                playerNames += it.name
            }
            is GameEvent.PlayerExecuted -> playerStatuses[event.executed.name] = PlayerStatus.EXECUTED
            is GameEvent.PlayerAttacked -> playerStatuses[event.attacked.name] = PlayerStatus.ATTACKED
            is GameEvent.Divined -> divineResults[event.target.name] = event.result.displayName
            is GameEvent.MediumRevealed -> mediumResults[event.target.name] = event.result.displayName
            is GameEvent.StatementMade -> when (val stmt = event.statement) {
                is Statement.DivinationReport ->
                    divineReports += ReportEntry(event.speakerName, stmt.target.name, stmt.result.displayName)
                is Statement.MediumReport ->
                    mediumReports += ReportEntry(event.speakerName, stmt.target.name, stmt.result.displayName)
                else -> Unit
            }
            else -> Unit
        }
    }

    fun summary(): SurvivalView = SurvivalView(playerStatuses.toMap())

    fun divinationSummary(): DivinationView = DivinationView(
        playerNames = playerNames.toList(),
        divineResults = divineResults.toMap(),
        mediumResults = mediumResults.toMap(),
        divineReports = divineReports.toList(),
        mediumReports = mediumReports.toList(),
    )
}
