package werewolf

import werewolf.game.GameOverSignal
import werewolf.phase.Epilogue
import werewolf.phase.InitialPhase
import werewolf.phase.Phase
import werewolf.lodge.GeminiLodge
import werewolf.lodge.HonestCpuLodge
import werewolf.lodge.Lodge
import werewolf.lodge.PocAiLodge
import werewolf.lodge.RandomCpuLodge
import werewolf.lodge.RoleAwareCpuLodge
import werewolf.lodge.RollerCpuLodge
import werewolf.lodge.SmallLodge

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
    println("Lodgeを選択してください: AllHuman / RollerCPU / RandomCPU / HonestCPU / RoleAwareCPU / PocAI / Gemini")
    return when (readLine()?.trim()) {
        "RollerCPU"    -> RollerCpuLodge
        "RandomCPU"    -> RandomCpuLodge
        "HonestCPU"    -> HonestCpuLodge
        "RoleAwareCPU" -> RoleAwareCpuLodge
        "PocAI"        -> PocAiLodge
        "Gemini"       -> GeminiLodge
        else           -> SmallLodge
    }
}
