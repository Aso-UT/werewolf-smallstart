package werewolf.game

// 行動を決めたときの付随情報（AIの呼び出しで使ったトークン数など）。中身は判断方式ごとに異なる
interface ActionMetadata {
    fun toDisplayString(): String
    fun toSummable(): SummableMetadata
}

// 行動ごとの付随情報を、同じ判断方式どうしで合算できる形にしたもの
interface SummableMetadata {
    // その行動を何がどう決めたか。種類が違うものどうしで重ならないよう、種類名から始める
    val decisionMethod: String
    // 何件分の行動を合算したか
    val count: Int
    operator fun plus(other: SummableMetadata): SummableMetadata
    fun toDisplayString(): String
}

// 呼び出しに失敗し、既定の行動で代えたとき。失敗した呼び出しの付随情報は InvalidAiInput 側が持つ
object FallbackActionMetadata : ActionMetadata {
    override fun toDisplayString() = ""
    override fun toSummable() = FallbackSummableMetadata(count = 1)
}

data class FallbackSummableMetadata(override val count: Int) : SummableMetadata {
    override val decisionMethod = "回答取得失敗"
    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is FallbackSummableMetadata) { "Cannot sum $decisionMethod with ${other.decisionMethod}" }
        return FallbackSummableMetadata(count + other.count)
    }
    override fun toDisplayString() = ""
}

// 付随情報の中身をまだ作っていない判断方式（CPUなど）
object UnimplementedActionMetadata : ActionMetadata {
    override fun toDisplayString() = ""
    override fun toSummable() = UnimplementedSummableMetadata(count = 1)
}

data class UnimplementedSummableMetadata(override val count: Int) : SummableMetadata {
    override val decisionMethod = "未実装"
    override fun plus(other: SummableMetadata): SummableMetadata {
        require(other is UnimplementedSummableMetadata) { "Cannot sum $decisionMethod with ${other.decisionMethod}" }
        return UnimplementedSummableMetadata(count + other.count)
    }
    override fun toDisplayString() = ""
}
