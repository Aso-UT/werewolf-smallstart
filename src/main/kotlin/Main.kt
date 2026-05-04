package org.example

fun main() {
    val setup = getLodge().create()
    var phase: Phase = InitialPhase(setup.playerManager, setup.oracle)
    try {
        while (true) { phase = phase.proceed() }
    } catch (signal: GameOverSignal) {
        Epilogue(setup.playerManager, setup.oracle, signal).perform()
    }
}

private fun getLodge(): Lodge {
    println("Lodgeを選択してください: AllHuman / RollerCPU / RandomCPU / HonestCPU / RoleAwareCPU")
    return when (readLine()?.trim()) {
        "RollerCPU"   -> RollerCpuLodge
        "RandomCPU"   -> RandomCpuLodge
        "HonestCPU"   -> HonestCpuLodge
        "RoleAwareCPU" -> RoleAwareCpuLodge
        else -> SmallLodge
    }
}
