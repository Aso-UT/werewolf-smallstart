package werewolf.human

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.view.ChoiceView

interface HumanIO {
    fun display(view: RecallView)
    fun promptChoice(view: ChoiceView): String
    fun promptFreeText(title: String, description: String): String
    fun watchEpilogue(chronicles: List<ChronicleView>)
}
