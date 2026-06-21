package werewolf.game

sealed class RecallView {
    data class Observation(val category: String, val content: String) : RecallView()
    data class Action(val category: String, val content: String, val intent: String) : RecallView()
}
