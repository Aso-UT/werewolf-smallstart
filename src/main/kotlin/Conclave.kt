package org.example

open class Conclave(wolves: List<Player>) : Discussion(wolves, AllPlayers(wolves)) {
    override fun speakingOrder(players: List<Player>) = players.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: String, recipients: AllPlayers) =
        GameEvent.WerewolfStatementMade.send(round, speakerName, statement, recipients)
}