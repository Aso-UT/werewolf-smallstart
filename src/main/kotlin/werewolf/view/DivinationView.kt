package werewolf.view

data class ReportEntry(
    val source: String,
    val targetName: String,
    val result: String,
    val trusted: Boolean,
)

data class DivinationView(
    val playerNames: List<String>,
    val divineReports: List<ReportEntry>,
    val mediumReports: List<ReportEntry>,
)
