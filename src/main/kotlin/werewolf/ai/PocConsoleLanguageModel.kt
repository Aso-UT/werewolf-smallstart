package werewolf.ai


class PocConsoleLanguageModel : LanguageModel {
    override fun ask(system: String, user: String): Completion {
        println()
        println("=".repeat(SEPARATOR_WIDTH))
        println("【ゲームの説明】")
        println(system)
        println()
        println(user)
        println("=".repeat(SEPARATOR_WIDTH))
        print("回答 > ")
        return Completion(readLine()?.trim() ?: "", ModelMetadata { "不明（コンソール経由のため）" })
    }

    companion object {
        private const val SEPARATOR_WIDTH = 50
    }
}
