package werewolf.game

abstract class Recallable {
    val sequenceId: Long = System.nanoTime()
    abstract fun recall(): String
    abstract fun chronicle(): String
    open val intentForChronicle: String? get() = null
    open val isRedundantInChronicle: Boolean get() = false
}
