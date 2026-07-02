package werewolf.view

data class ReportEntry(val source: String, val targetName: String, val isWerewolf: Boolean)

data class DivinationView(
    val playerNames: List<String>,
    val divineResults: Map<String, Boolean>,
    val mediumResults: Map<String, Boolean>,
    val divineReports: List<ReportEntry>,
    val mediumReports: List<ReportEntry>,
)
