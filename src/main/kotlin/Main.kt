package org.example

fun main() {
    val setup = SmallLodge.create()
    val manager = PlayerManager(setup)
    var phase: Phase = InitialPhase(manager, setup.oracle)
    try {
        while (true) { phase = phase.proceed() }
    } catch (signal: GameOverSignal) {
        EndPhase(manager, setup.oracle, signal).proceed()
    }
}