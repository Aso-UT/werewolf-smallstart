package org.example

abstract class Lodge {
    abstract fun assignments(): List<Pair<Player, Role>>

    fun create(): GameSetup {
        val assignments = assignments()
        return GameSetup(
            players = assignments.map { it.first },
            oracle = Oracle(assignments.toMap()),
        )
    }
}
