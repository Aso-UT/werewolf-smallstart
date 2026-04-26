package org.example

fun main() {
    val setup = getLodge().create()
    val manager = PlayerManager(setup)
    var phase: Phase = InitialPhase(manager, setup.oracle)
    try {
        while (true) { phase = phase.proceed() }
    } catch (signal: GameOverSignal) {
        EndPhase(manager, setup.oracle, signal).proceed()
    }
}

private fun getLodge(): Lodge {
    println("Lodgeを選択してください: AllHuman / RollerCPU")
    return when (readLine()?.trim()) {
        "RollerCPU" -> RollerCpuLodge
        else -> SmallLodge
    }
}