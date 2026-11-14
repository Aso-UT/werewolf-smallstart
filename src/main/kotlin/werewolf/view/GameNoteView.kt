package werewolf.view

// 人間プレイヤーのメモを、プレイヤーごとの行にまとめたもの
data class GameNoteView(
    // 参加順
    val players: List<PlayerNote>,
    // 自分が占い・霊視で得た結果の列の見出し（「占い結果」か「霊能結果」）。まだ結果を得ていなければ null
    val ownResultTitle: String?,
    // 占い結果を報告した人。役職申告か結果の報告で、役職を最初に明かした順
    val divinationReporters: List<String>,
    // 霊能結果を報告した人。並び順は占いと同じ
    val mediumReporters: List<String>,
)

data class PlayerNote(
    val name: String,
    val status: PlayerStatus,
    val claimedRole: String?,
    val knownRole: String?,
    // 自分が占い・霊視で得た本当の結果。true は人狼
    val ownResult: Boolean?,
    // 占い結果を報告した人の名前から、その人がこのプレイヤーについて報告した結果へ。true は人狼
    val divinationReports: Map<String, Boolean>,
    val mediumReports: Map<String, Boolean>,
)
