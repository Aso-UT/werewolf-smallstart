package werewolf.game

// 議論で「今どれだけ発言したいか」を表す。値が大きいほど発言したい
data class Eagerness(val value: Int) : Comparable<Eagerness> {
    override fun compareTo(other: Eagerness) = value.compareTo(other.value)

    // この意欲で発言権を得たときに選べる発言の種類
    val statementTypes: Set<StatementType>
        get() = if (this >= ANNOUNCEMENT) ANNOUNCEMENT_TYPES else StatementType.entries.toSet()

    companion object {
        val COUNTER_CLAIM = Eagerness(5)
        val ANNOUNCEMENT = Eagerness(4)
        val URGENT = Eagerness(3)
        val OPINION = Eagerness(2)
        val NOTHING = Eagerness(1)
        val UNWILLING = Eagerness(0)

        val DEFAULT = OPINION

        private val ANNOUNCEMENT_TYPES = setOf(StatementType.ROLE_CLAIM, StatementType.DIVINATION_REPORT, StatementType.MEDIUM_REPORT)
    }
}
