package org.example

class InitialPhase(private val oracle: Oracle) : Phase {
    override fun proceed() = oracle.initiatePlayers()
}
