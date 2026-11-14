package werewolf.view

// 人間プレイヤーのメモを、プレイヤーごとの行にまとめたもの
data class GameNoteView(
    // 参加順
    val players: List<PlayerNote>,
    // 占い師を名乗った人。役職申告か占い結果の報告で、最初に名乗った順
    val seerClaimants: List<String>,
    // 霊能者を名乗った人。並び順は占い師と同じ
    val mediumClaimants: List<String>,
    // 自分が占い師か霊能者で、自分で得た結果があり得るか
    val hasOwnResults: Boolean,
)

data class PlayerNote(
    val name: String,
    val status: PlayerStatus,
    val claimedRole: String?,
    val knownRole: String?,
    // 自分が占い・霊視で得た本当の結果。true は人狼
    val ownResult: Boolean?,
    // 占い師を名乗った人の名前から、その人がこのプレイヤーについて報告した結果へ。true は人狼
    val divinationReports: Map<String, Boolean>,
    val mediumReports: Map<String, Boolean>,
)
