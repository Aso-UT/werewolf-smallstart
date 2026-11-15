package werewolf.game

// 議論の一区切り。一人が発言するか、複数人が同時に発言する。
// 同時の発言に前後はないので、並びに意味を持たせないよう Iterable として扱う。
// DiscussionContext.round（その人にとって何回目の発言か）とは別の数え方
class Turn(private val statements: List<Statement>) : Iterable<Statement> by statements {
    constructor(vararg statements: Statement) : this(statements.toList())

    init {
        require(statements.isNotEmpty()) { "A turn needs at least one statement" }
    }

    val isSimultaneous: Boolean get() = statements.size > 1

    val speakers: List<Player> get() = statements.map { it.claimant }

    fun body(): String =
        statements.singleOrNull()?.text() ?: joinToString("\n") { "${it.claimant.name}: ${it.text()}" }

    fun toRecallView(title: String): RecallView =
        statements.singleOrNull()?.let { RecallView.AttributedObservation(title, it.claimant.name, it.text()) }
            ?: RecallView.SimultaneousRemarks(title, map { Remark(it.claimant.name, it.text()) })

    // 一人の発言は各人のClaimでエピローグに載るので、ここで載せるのは同時に発言したという事実を残すため
    fun toChronicleView(title: String, recipientName: String): ChronicleView {
        check(isSimultaneous) { "A single statement is excluded from chronicles by GameRecap.isRedundantWithClaim" }
        return ChronicleView.Observation(recipientName, "${title}（同時発言）", body())
    }
}
