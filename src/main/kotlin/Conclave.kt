package org.example

open class Conclave(oracle: Oracle, playerManager: PlayerManager) :
    Discussion(oracle.werewolves(playerManager.players)) {
    private val wolves = Wolves(oracle, playerManager)
    override val rounds = 3
    override fun speakingOrder(speakers: List<Player>) = speakers.shuffled()
    override fun sendStatement(round: Int, speakerName: String, statement: Statement) =
        GameEvent.WerewolfStatementMade.send(round, speakerName, statement.text(), wolves)
}
