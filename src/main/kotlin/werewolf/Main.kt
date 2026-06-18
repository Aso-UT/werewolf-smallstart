package werewolf

import werewolf.game.GameOverSignal
import werewolf.lodge.AnthropicLodge
import werewolf.lodge.GeminiLodge
import werewolf.lodge.HonestCpuLodge
import werewolf.lodge.Lodge
import werewolf.lodge.PocAiLodge
import werewolf.lodge.RandomCpuLodge
import werewolf.lodge.RoleAwareCpuLodge
import werewolf.lodge.RollerCpuLodge
import werewolf.lodge.SmallLodge
import werewolf.lodge.WebLodge
import werewolf.phase.Epilogue
import werewolf.phase.InitialPhase
import werewolf.phase.Phase

fun main() {
    val lodge = getLodge()
    val setup = lodge.create()
    lodge.setup()
    try {
        var phase: Phase = InitialPhase(setup.playerManager, setup.oracle)
        while (true) { phase = phase.proceed() }
    } catch (signal: GameOverSignal) {
        Epilogue(setup.playerManager, setup.oracle, signal).perform()
    }
    lodge.teardown()
}

private fun getLodge(): Lodge {
    println("Lodgeを選択してください: AllHuman / RollerCPU / RandomCPU / HonestCPU / RoleAwareCPU / PocAI / Gemini / Anthropic / Web")
    return when (readLine()?.trim()) {
        "RollerCPU"    -> RollerCpuLodge
        "RandomCPU"    -> RandomCpuLodge
        "HonestCPU"    -> HonestCpuLodge
        "RoleAwareCPU" -> RoleAwareCpuLodge
        "PocAI"        -> PocAiLodge
        "Gemini"       -> GeminiLodge
        "Anthropic"    -> AnthropicLodge
        "Web"          -> WebLodge()
        else           -> SmallLodge
    }
}
