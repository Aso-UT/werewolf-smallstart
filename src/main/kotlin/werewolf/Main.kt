package werewolf

import werewolf.game.GameOverSignal
import werewolf.lodge.AnthropicLodge
import werewolf.lodge.ConsoleHumanConnection
import werewolf.lodge.GeminiLodge
import werewolf.lodge.HonestCpuLodge
import werewolf.lodge.HumanConnection
import werewolf.lodge.Lodge
import werewolf.lodge.PocAiLodge
import werewolf.lodge.RandomCpuLodge
import werewolf.lodge.RoleAwareCpuLodge
import werewolf.lodge.RollerCpuLodge
import werewolf.lodge.SmallLodge
import werewolf.lodge.WebHumanConnection
import werewolf.phase.Epilogue
import werewolf.phase.InitialPhase
import werewolf.phase.Phase

fun main() {
    val lodge = selectLodge(selectHumanConnection())
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

private fun selectLodge(connection: HumanConnection): Lodge {
    println("Lodgeを選択してください: AllHuman / RollerCPU / RandomCPU / HonestCPU / RoleAwareCPU / PocAI / Gemini / Anthropic")
    return when (readLine()?.trim()) {
        "RollerCPU"    -> RollerCpuLodge(connection)
        "RandomCPU"    -> RandomCpuLodge(connection)
        "HonestCPU"    -> HonestCpuLodge(connection)
        "RoleAwareCPU" -> RoleAwareCpuLodge(connection)
        "PocAI"        -> PocAiLodge(connection)
        "Gemini"       -> GeminiLodge(connection)
        "Anthropic"    -> AnthropicLodge(connection)
        else           -> SmallLodge(connection)
    }
}

private fun selectHumanConnection(): HumanConnection {
    println("接続方法を選択してください: Console / Web")
    return when (readLine()?.trim()) {
        "Web"  -> WebHumanConnection()
        else   -> ConsoleHumanConnection()
    }
}
