package werewolf.game

// 議論で「今どれだけ発言したいか」を表す。値が大きいほど発言したい
data class Eagerness(val value: Int) : Comparable<Eagerness> {
    override fun compareTo(other: Eagerness) = value.compareTo(other.value)

    // AIは1〜4の4段階から選ぶ（段階の意味はUrgeFormatを参照）
    companion object {
        // 対抗COに間に合うよう、最上位の段階と同じにする
        val HIGH = Eagerness(4)
        val DEFAULT = Eagerness(2)
        // 発言を望まない人は、AIのどの段階よりも後に回す
        val LOW = Eagerness(0)
    }
}
