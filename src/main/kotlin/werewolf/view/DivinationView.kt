package werewolf.view

data class ReportEntry(val source: String, val targetName: String, val result: String)

data class DivinationView(
    val playerNames: List<String>,
    val divineResults: Map<String, String>,
    val mediumResults: Map<String, String>,
    val divineReports: List<ReportEntry>,
    val mediumReports: List<ReportEntry>,
)
