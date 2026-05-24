package werewolf.ai


fun interface LanguageModel {
    fun ask(system: String, user: String): Completion
}
