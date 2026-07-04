package werewolf.human

import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.MediumResult
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.view.DivinationView
import werewolf.view.PlayerStatus
import werewolf.view.PlayerStatusView
import werewolf.view.PlayerSummary
import werewolf.view.ReportEntry

class GameNote {
    private val playerStatuses = mutableMapOf<String, PlayerStatus>()
    private val playerNames = mutableListOf<String>()
    private val divineResults = mutableMapOf<String, Boolean>()
    private val mediumResults = mutableMapOf<String, Boolean>()
    private val divineReports = mutableSetOf<ReportEntry>()
    private val mediumReports = mutableSetOf<ReportEntry>()
    private val claimedRoles = mutableMapOf<String, String>()

    fun post(event: GameEvent) {
        when (event) {
            is GameEvent.PlayersAnnounced -> event.players.forEach {
                playerStatuses[it.name] = PlayerStatus.ALIVE
                playerNames += it.name
            }
            is GameEvent.PlayerExecuted -> playerStatuses[event.executed.name] = PlayerStatus.EXECUTED
            is GameEvent.PlayerAttacked -> playerStatuses[event.attacked.name] = PlayerStatus.ATTACKED
            is GameEvent.Divined -> divineResults[event.target.name] = event.result == DivineResult.WEREWOLF
            is GameEvent.MediumRevealed -> mediumResults[event.target.name] = event.result == MediumResult.WEREWOLF
            is GameEvent.StatementMade -> when (val stmt = event.statement) {
                is Statement.DivinationReport -> {
                    divineReports += ReportEntry(event.speakerName, stmt.target.name, stmt.result == DivineResult.WEREWOLF)
                    claimedRoles[event.speakerName] = Role.SEER.displayName
                }
                is Statement.MediumReport -> {
                    mediumReports += ReportEntry(event.speakerName, stmt.target.name, stmt.result == MediumResult.WEREWOLF)
                    claimedRoles[event.speakerName] = Role.MEDIUM.displayName
                }
                is Statement.RoleClaim -> claimedRoles[event.speakerName] = stmt.role.displayName
                else -> Unit
            }
            else -> Unit
        }
    }

    fun playerStatusSummary(): PlayerStatusView = PlayerStatusView(
        playerNames.map { name -> PlayerSummary(name, playerStatuses.getValue(name), claimedRoles[name]) },
    )

    fun divinationSummary(): DivinationView = DivinationView(
        playerNames = playerNames.toList(),
        divineResults = divineResults.toMap(),
        mediumResults = mediumResults.toMap(),
        divineReports = divineReports.toList(),
        mediumReports = mediumReports.toList(),
    )
}
