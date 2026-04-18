package org.example

sealed class TimeOfDay {
    abstract val name: String
    abstract val displayName: String

    data class Night(val nightNumber: Int) : TimeOfDay() {
        override val name = "夜"
        override val displayName = "${nightNumber}日目の夜"
    }

    data object Morning : TimeOfDay() {
        override val name = "朝"
        override val displayName = "朝"
    }

    data class Midnight(val nightNumber: Int) : TimeOfDay() {
        override val name = "深夜"
        override val displayName = "深夜"
    }
}
