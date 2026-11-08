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
    private val knownRoles = mutableMapOf<String, String>()

    fun post(event: GameEvent) {
        when (event) {
            is GameEvent.PlayersAnnounced -> event.players.forEach {
                playerStatuses[it.name] = PlayerStatus.ALIVE
                playerNames += it.name
            }
            is GameEvent.RoleAssigned -> knownRoles[event.assignee.name] = event.role.displayName
            is GameEvent.WerewolfAllyRevealed -> knownRoles[event.ally.name] = Role.WEREWOLF.displayName
            is GameEvent.RolesRevealed -> event.roles.forEach { (player, role) -> knownRoles[player.name] = role.displayName }
            is GameEvent.PlayerExecuted -> playerStatuses[event.executed.name] = PlayerStatus.EXECUTED
            is GameEvent.PlayerAttacked -> playerStatuses[event.attacked.name] = PlayerStatus.ATTACKED
            is GameEvent.Divined -> divineResults[event.target.name] = event.result == DivineResult.WEREWOLF
            is GameEvent.MediumRevealed -> mediumResults[event.target.name] = event.result == MediumResult.WEREWOLF
            is GameEvent.StatementMade -> noteStatement(event.speakerName, event.statement)
            else -> Unit
        }
    }

    private fun noteStatement(speakerName: String, statement: Statement) {
        when (statement) {
            is Statement.DivinationReport -> {
                divineReports += ReportEntry(speakerName, statement.target.name, statement.result == DivineResult.WEREWOLF)
                claimedRoles[speakerName] = Role.SEER.displayName
            }
            is Statement.MediumReport -> {
                mediumReports += ReportEntry(speakerName, statement.target.name, statement.result == MediumResult.WEREWOLF)
                claimedRoles[speakerName] = Role.MEDIUM.displayName
            }
            is Statement.RoleClaim -> claimedRoles[speakerName] = statement.role.displayName
            else -> Unit
        }
    }

    fun playerStatusSummary(): PlayerStatusView = PlayerStatusView(
        playerNames.map { name -> PlayerSummary(name, playerStatuses.getValue(name), claimedRoles[name], knownRoles[name]) },
    )

    fun divinationSummary(): DivinationView = DivinationView(
        playerNames = playerNames.toList(),
        divineResults = divineResults.toMap(),
        mediumResults = mediumResults.toMap(),
        divineReports = divineReports.toList(),
        mediumReports = mediumReports.toList(),
    )
}
