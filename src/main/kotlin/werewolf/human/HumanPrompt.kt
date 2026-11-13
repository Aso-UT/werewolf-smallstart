package werewolf.human

import werewolf.view.ChoiceView

interface HumanPrompt {
    // 問い合わせに答えられる状態になるまで待つ。応答時間に含めないよう、計測を始める前に呼ぶ
    fun waitUntilReady()
    fun promptChoice(view: ChoiceView): String
    fun promptFreeText(title: String, description: String): String
    // 押されたボタンの名前を返す。時間内に押されなければ、表示しない timeoutButton を押したことにする
    fun promptSpeakingUrge(title: String, seconds: Int, buttons: List<String>, timeoutButton: String): String
}
