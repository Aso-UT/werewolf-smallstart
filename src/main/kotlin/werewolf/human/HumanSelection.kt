package werewolf.human

import werewolf.util.Cardinality
import werewolf.util.cardinality
import werewolf.view.ChoiceView

class HumanSelection(private val io: HumanPrompt) {
    // 候補が一つしかなければ、選ばせずにそれに決める
    fun <T> select(title: String, description: String, candidates: List<T>, label: (T) -> String): T =
        when (val found = candidates.cardinality()) {
            Cardinality.None -> error("$title: there must be at least one candidate to select")
            is Cardinality.One -> found.value
            is Cardinality.Many -> promptChoice(title, description, found.values, label)
        }

    private fun <T> promptChoice(title: String, description: String, candidates: List<T>, label: (T) -> String): T {
        val selected = io.promptChoice(ChoiceView(title, description, candidates.map(label)))
        return candidates.single { label(it) == selected }
    }
}
