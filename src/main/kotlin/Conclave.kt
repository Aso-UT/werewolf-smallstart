package org.example

open class Conclave(wolves: List<Player>) : Discussion(wolves, AllPlayers(wolves)) {
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: String, recipients: AllPlayers) =
        GameEvent.WerewolfStatementMade.send(round, speakerName, statement, recipients)
}