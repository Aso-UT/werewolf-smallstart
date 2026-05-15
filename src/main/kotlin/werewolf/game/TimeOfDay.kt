package werewolf.game

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

}
