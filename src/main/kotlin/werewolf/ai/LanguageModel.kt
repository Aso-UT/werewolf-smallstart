package werewolf.ai


fun interface LanguageModel {
    fun ask(system: String, history: List<String>, instruction: String): Completion
}
