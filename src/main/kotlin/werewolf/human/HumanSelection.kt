package werewolf.human

import werewolf.view.ChoiceView

class HumanSelection(private val io: HumanPrompt) {
    fun <T> select(title: String, description: String, candidates: List<T>, label: (T) -> String): T =
        if (candidates.size == 1) candidates.single() else promptChoice(title, description, candidates, label)

    private fun <T> promptChoice(title: String, description: String, candidates: List<T>, label: (T) -> String): T {
        val selected = io.promptChoice(ChoiceView(title, description, candidates.map(label)))
        return candidates.single { label(it) == selected }
    }
}
