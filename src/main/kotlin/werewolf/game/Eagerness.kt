package werewolf.game

// 議論で「今どれだけ発言したいか」を表す。値が大きいほど発言したい
data class Eagerness(val value: Int) : Comparable<Eagerness> {
    override fun compareTo(other: Eagerness) = value.compareTo(other.value)

    companion object {
        val DEFAULT = Eagerness(0)
    }
}
