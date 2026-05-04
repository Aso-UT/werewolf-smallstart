package org.example

abstract class Lodge {
    abstract fun assignments(): List<Pair<Player, Role>>

    fun create(): GameSetup {
        val assignments = assignments()
        val oracle = Oracle(assignments.toMap())
        val playerManager = PlayerManager(assignments.map { it.first }, oracle)
        return GameSetup(playerManager, oracle)
    }
}
