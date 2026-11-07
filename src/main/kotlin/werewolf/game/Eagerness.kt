package werewolf.game

// 議論で「今どれだけ発言したいか」を表す。値が大きいほど発言したい
data class Eagerness(val value: Int) : Comparable<Eagerness> {
    override fun compareTo(other: Eagerness) = value.compareTo(other.value)

    // 具体的な値はAIの発言意欲のスケール（#310）が決まってから合わせる
    companion object {
        val HIGH = Eagerness(1)
        val DEFAULT = Eagerness(0)
        val LOW = Eagerness(-1)
    }
}
