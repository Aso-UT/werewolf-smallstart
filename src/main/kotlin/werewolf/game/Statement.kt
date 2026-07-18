package werewolf.game

sealed class Statement {
    abstract val claimant: Player
    abstract val type: StatementType
    abstract fun text(): String

    data class Plain(override val claimant: Player, private val content: String) : Statement() {
        override val type = StatementType.PLAIN
        override fun text() = content
    }

    data class DivinationReport(
        override val claimant: Player,
        val target: Player,
        val result: DivineResult,
        val comment: String = "",
    ) : Statement() {
        override val type = StatementType.DIVINATION_REPORT
        override fun text() = "${target.name}を占ったところ、「${result.displayName}」でした。".withComment(comment)
    }

    data class MediumReport(
        override val claimant: Player,
        val target: Player,
        val result: MediumResult,
        val comment: String = "",
    ) : Statement() {
        override val type = StatementType.MEDIUM_REPORT
        override fun text() = "${target.name}の霊能結果は、「${result.displayName}」でした。".withComment(comment)
    }

    data class RoleClaim(
        override val claimant: Player,
        val role: Role,
        val comment: String = "",
    ) : Statement() {
        override val type = StatementType.ROLE_CLAIM
        override fun text() = "私の役職は「${role.displayName}」です。".withComment(comment)
    }
}

private fun String.withComment(comment: String): String = if (comment.isBlank()) this else "$this $comment"
