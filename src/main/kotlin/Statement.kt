package org.example

sealed class Statement {
    abstract val type: StatementType
    abstract fun text(): String

    data class Plain(private val content: String) : Statement() {
        override val type = StatementType.PLAIN
        override fun text() = content
    }

    data class DivinationReport(val claimant: Player, val target: Player, val result: DivineResult) : Statement() {
        override val type = StatementType.DIVINATION_REPORT
        override fun text() = "${target.name} は「${result.displayName}」です。"
    }

    data class MediumReport(val claimant: Player, val target: Player, val result: MediumResult) : Statement() {
        override val type = StatementType.MEDIUM_REPORT
        override fun text() = "${target.name} は「${result.displayName}」です。"
    }
}
