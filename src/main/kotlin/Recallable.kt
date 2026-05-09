package org.example

abstract class Recallable {
    val sequenceId: Long = System.nanoTime()
    abstract fun recall(): String
    abstract fun chronicle(): String
}
