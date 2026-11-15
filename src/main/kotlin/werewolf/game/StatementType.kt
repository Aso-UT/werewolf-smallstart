package werewolf.game

// claimedRole は、この種類の発言で名乗る役職。結果報告は、その結果を得られる役職のCOとみなす
sealed class StatementType(val displayName: String, val claimedRole: Role?) {
    data object Plain : StatementType("発言する", null)
    data object DivinationReport : StatementType("占い結果を報告する", Role.SEER)
    data object MediumReport : StatementType("霊媒結果を報告する", Role.MEDIUM)
    data class RoleClaim(val role: Role) : StatementType("役職を開示する", role)

    companion object {
        // 種類の初期化中に親クラスの初期化が走るため、すぐには作らずに初めて使うときに作る
        val all: Set<StatementType> by lazy { setOf(Plain, DivinationReport, MediumReport) + Role.entries.map { RoleClaim(it) } }
    }
}
