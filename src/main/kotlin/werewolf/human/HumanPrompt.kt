package werewolf.human

import werewolf.view.ChoiceView

interface HumanPrompt {
    // 問い合わせに答えられる状態になるまで待つ。応答時間に含めないよう、計測を始める前に呼ぶ
    fun waitUntilReady()
    fun promptChoice(view: ChoiceView): String
    fun promptFreeText(title: String, description: String): String
    fun promptSpeakingUrge(title: String, seconds: Int): Boolean
}
