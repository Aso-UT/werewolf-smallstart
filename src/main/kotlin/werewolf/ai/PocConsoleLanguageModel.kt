package werewolf.ai


class PocConsoleLanguageModel : LanguageModel {
    override fun ask(system: String, user: String): String {
        println()
        println("=".repeat(SEPARATOR_WIDTH))
        println("【ゲームの説明】")
        println(system)
        println()
        println(user)
        println("=".repeat(SEPARATOR_WIDTH))
        print("回答 > ")
        return readLine()?.trim() ?: ""
    }

    companion object {
        private const val SEPARATOR_WIDTH = 50
    }
}
