package werewolf.human

import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.MediumResult
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.view.DivinationView
import werewolf.view.PlayerStatus
import werewolf.view.ReportEntry
import werewolf.view.RoleClaimEntry
import werewolf.view.RoleClaimView
import werewolf.view.SurvivalView

class GameNote {
    private val playerStatuses = mutableMapOf<String, PlayerStatus>()
    private val playerNames = mutableListOf<String>()
    private val divineResults = mutableMapOf<String, Boolean>()
    private val mediumResults = mutableMapOf<String, Boolean>()
    private val divineReports = mutableSetOf<ReportEntry>()
    private val mediumReports = mutableSetOf<ReportEntry>()
    private val roleClaims = mutableSetOf<RoleClaimEntry>()

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
                    roleClaims += RoleClaimEntry(event.speakerName, Role.SEER.displayName)
                }
                is Statement.MediumReport -> {
                    mediumReports += ReportEntry(event.speakerName, stmt.target.name, stmt.result == MediumResult.WEREWOLF)
                    roleClaims += RoleClaimEntry(event.speakerName, Role.MEDIUM.displayName)
                }
                is Statement.RoleClaim -> roleClaims += RoleClaimEntry(event.speakerName, stmt.role.displayName)
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

    fun roleClaimSummary(): RoleClaimView = RoleClaimView(roleClaims.toList())
}
