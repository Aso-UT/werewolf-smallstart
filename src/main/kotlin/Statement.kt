package org.example

sealed class Statement {
    abstract fun text(): String

    data class Plain(private val content: String) : Statement() {
        override fun text() = content
    }

    data class DivinationReport(val claimant: Player, val target: Player, val result: DivineResult) : Statement() {
        override fun text() = "${target.name} は「${result.displayName}」です。"
    }

    data class MediumReport(val claimant: Player, val target: Player, val result: MediumResult) : Statement() {
        override fun text() = "${target.name} は「${result.displayName}」です。"
    }
}
