package org.example

open class Conclave(oracle: Oracle, playerManager: PlayerManager) :
    Discussion<Wolves>(oracle.werewolves(playerManager.players), Wolves(oracle, playerManager)) {
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: Statement, recipients: Wolves) =
        GameEvent.WerewolfStatementMade.send(round, speakerName, statement.text(), recipients)
}
