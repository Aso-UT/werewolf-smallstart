package werewolf.game

// reporterRole は、この種類の結果報告ができる役職
enum class StatementType(val displayName: String, val reporterRole: Role?) {
    PLAIN("発言する", null),
    DIVINATION_REPORT("占い結果を報告する", Role.SEER),
    MEDIUM_REPORT("霊媒結果を報告する", Role.MEDIUM),
    ROLE_CLAIM("役職を開示する", null),
}
