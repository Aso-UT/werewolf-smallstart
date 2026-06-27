package werewolf.human

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.view.ChoiceView

abstract class HumanIO {
    abstract fun display(view: RecallView)
    abstract fun promptChoice(view: ChoiceView): String
    abstract fun promptFreeText(title: String, description: String): String
    abstract fun watchEpilogue(chronicles: List<ChronicleView>)
}
