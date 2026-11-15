package werewolf.game

sealed class RecallView {
    data class Observation(val category: String, val content: String) : RecallView()
    data class AttributedObservation(val category: String, val actor: String, val content: String) : RecallView()
    // 複数人が同時にした発言。並びは時間の前後を意味しない
    data class SimultaneousRemarks(val category: String, val remarks: List<Remark>) : RecallView()
    data class SelfAction(val category: String, val content: String, val intent: String) : RecallView()
}

data class Remark(val actor: String, val content: String)
