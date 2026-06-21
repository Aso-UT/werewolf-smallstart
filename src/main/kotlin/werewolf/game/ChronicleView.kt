package werewolf.game

sealed class ChronicleView {
    data class Observation(val recipient: String, val category: String, val content: String) : ChronicleView()
    data class Action(val actor: String, val category: String, val content: String, val intent: String) : ChronicleView()
}
