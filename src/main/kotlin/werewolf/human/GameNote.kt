package werewolf.human

import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.MediumResult
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.view.GameNoteView
import werewolf.view.PlayerNote
import werewolf.view.PlayerStatus

class GameNote {
    private val playerStatuses = mutableMapOf<String, PlayerStatus>()
    private val playerNames = mutableListOf<String>()
    private val divineResults = mutableMapOf<String, Boolean>()
    private val mediumResults = mutableMapOf<String, Boolean>()
    private val divineReports = mutableSetOf<ReportEntry>()
    private val mediumReports = mutableSetOf<ReportEntry>()
    private val claimedRoles = mutableMapOf<String, String>()
    // 挿入順を保つので、役職を最初に明かした順に並ぶ
    private val seerClaimants = linkedSetOf<String>()
    private val mediumClaimants = linkedSetOf<String>()
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
            is GameEvent.StatementMade -> event.statements.forEach { noteStatement(it.claimant.name, it) }
            else -> Unit
        }
    }

    private fun noteStatement(speakerName: String, statement: Statement) {
        statement.claimedRole?.let { noteClaim(speakerName, it) }
        when (statement) {
            is Statement.DivinationReport ->
                divineReports += ReportEntry(speakerName, statement.target.name, statement.result == DivineResult.WEREWOLF)
            is Statement.MediumReport ->
                mediumReports += ReportEntry(speakerName, statement.target.name, statement.result == MediumResult.WEREWOLF)
            else -> Unit
        }
    }

    private fun noteClaim(speakerName: String, role: Role) {
        claimedRoles[speakerName] = role.displayName
        when (role) {
            Role.SEER -> seerClaimants += speakerName
            Role.MEDIUM -> mediumClaimants += speakerName
            else -> Unit
        }
    }

    fun summary(): GameNoteView = GameNoteView(
        players = playerNames.map { name ->
            PlayerNote(
                name = name,
                status = playerStatuses.getValue(name),
                claimedRole = claimedRoles[name],
                knownRole = knownRoles[name],
                ownResult = divineResults[name] ?: mediumResults[name],
                divinationReports = reportsOn(name, divineReports),
                mediumReports = reportsOn(name, mediumReports),
            )
        },
        ownResultTitle = when {
            divineResults.isNotEmpty() -> "占い結果"
            mediumResults.isNotEmpty() -> "霊能結果"
            else -> null
        },
        divinationReporters = reportersIn(seerClaimants, divineReports),
        mediumReporters = reportersIn(mediumClaimants, mediumReports),
    )

    // 名乗っただけの人は除き、結果を報告した人だけを、役職を最初に明かした順に並べる
    private fun reportersIn(claimants: Set<String>, reports: Set<ReportEntry>): List<String> =
        claimants.filter { claimant -> reports.any { it.source == claimant } }

    private fun reportsOn(targetName: String, reports: Set<ReportEntry>): Map<String, Boolean> =
        reports.filter { it.targetName == targetName }.associate { it.source to it.isWerewolf }

    private data class ReportEntry(val source: String, val targetName: String, val isWerewolf: Boolean)
}
