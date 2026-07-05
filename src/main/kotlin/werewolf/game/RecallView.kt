package werewolf.game

sealed class RecallView {
    data class Observation(val category: String, val content: String) : RecallView()
    data class AttributedObservation(val category: String, val actor: String, val content: String) : RecallView()
    data class SelfAction(val category: String, val content: String, val intent: String) : RecallView()
}
