package werewolf.ai.poc

import werewolf.ai.Completion
import werewolf.ai.LanguageModel
import werewolf.ai.RuminationRecord
import werewolf.game.UnimplementedActionMetadata

class PocConsoleLanguageModel : LanguageModel {
    override fun ask(system: String, history: List<String>, instruction: String): Completion {
        val user = buildString {
            if (history.isNotEmpty()) {
                appendLine("【ここまでのゲームの流れ】")
                history.forEach { appendLine(it) }
                appendLine()
            }
            append(instruction)
        }
        println()
        println("=".repeat(SEPARATOR_WIDTH))
        println("【ゲームの説明】")
        println(system)
        println()
        println(user)
        println("=".repeat(SEPARATOR_WIDTH))
        print("回答 > ")
        return Completion(readLine()?.trim() ?: "", UnimplementedActionMetadata)
    }

    // 人がコンソールで答えるので、キャッシュを保つ反芻はしない
    override fun drainRuminations(): List<RuminationRecord> = emptyList()
    override fun stop() { /* no ruminations to stop */ }

    companion object {
        private const val SEPARATOR_WIDTH = 50
    }
}
