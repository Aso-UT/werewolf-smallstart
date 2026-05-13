package werewolf.ai


interface LanguageModel {
    fun ask(prompt: String): String
}
