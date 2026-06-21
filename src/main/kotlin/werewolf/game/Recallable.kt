package werewolf.game

abstract class Recallable {
    val sequenceId: Long = System.nanoTime()
    abstract fun toRecallView(): RecallView
    abstract fun toChronicleView(): ChronicleView
}
