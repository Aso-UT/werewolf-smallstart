package werewolf.game

import werewolf.util.Cardinality
import werewolf.util.cardinality

// 議論の一区切り。一人が発言するか、複数人が同時に発言する。
// 同時の発言に前後はないので、並びに意味を持たせないよう Iterable として扱う。
// DiscussionContext.round（その人にとって何回目の発言か）とは別の数え方
class Turn(private val statements: List<Statement>) : Iterable<Statement> by statements {
    constructor(vararg statements: Statement) : this(statements.toList())

    private val speech: Cardinality<Statement> = statements.cardinality()

    init {
        require(speech != Cardinality.None) { "A turn needs at least one statement" }
    }

    val isSimultaneous: Boolean get() = speech is Cardinality.Many

    val speakers: List<Player> get() = statements.map { it.claimant }

    fun body(): String = when (speech) {
        Cardinality.None -> neverEmpty()
        is Cardinality.One -> speech.value.text()
        is Cardinality.Many -> speech.values.joinToString("\n") { "${it.claimant.name}: ${it.text()}" }
    }

    fun toRecallView(title: String): RecallView = when (speech) {
        Cardinality.None -> neverEmpty()
        is Cardinality.One -> RecallView.AttributedObservation(title, speech.value.claimant.name, speech.value.text())
        is Cardinality.Many -> RecallView.SimultaneousRemarks(title, speech.values.map { Remark(it.claimant.name, it.text()) })
    }

    // 一人の発言は各人のClaimでエピローグに載るので、ここで載せるのは同時に発言したという事実を残すため
    fun toChronicleView(title: String, recipientName: String): ChronicleView {
        check(isSimultaneous) { "A single statement is excluded from chronicles by GameRecap.isRedundantWithClaim" }
        return ChronicleView.Observation(recipientName, "${title}（同時発言）", body())
    }

    private fun neverEmpty(): Nothing = error("A turn is never empty")
}
