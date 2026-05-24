package werewolf.ai


interface LanguageModel {
    fun ask(system: String, user: String): Completion
}
