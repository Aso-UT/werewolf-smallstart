package werewolf.game

// プレイヤーが行動した記録。行動を決めたときの付随情報を持つ
interface PlayerAction {
    val metadata: ActionMetadata
}

// 振り返りでは、行動の理由に付随情報を添えて見せる
internal fun PlayerAction.intentWithMetadata(intent: String): String {
    val display = metadata.toDisplayString()
    return if (display.isEmpty()) intent else "$intent | $display"
}
