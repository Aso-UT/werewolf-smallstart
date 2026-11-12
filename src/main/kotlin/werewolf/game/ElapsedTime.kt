package werewolf.game

import java.util.Locale
import kotlin.time.Duration

// ゲーム内で何かにかかった時間を、プレイヤー向けに「3分12秒」の形で表す。1分未満は「45秒」とする
fun Duration.toMinutesSecondsString(): String = toComponents { minutes, seconds, _ ->
    if (minutes == 0L) "${seconds}秒" else String.format(Locale.ROOT, "%d分%02d秒", minutes, seconds)
}
