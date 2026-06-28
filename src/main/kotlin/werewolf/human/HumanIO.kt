package werewolf.human

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.view.ChoiceView
import werewolf.view.SurvivalView

interface HumanIO {
    fun display(view: RecallView)
    fun updatePanel(view: SurvivalView)
    fun promptChoice(view: ChoiceView): String
    fun promptFreeText(title: String, description: String): String
    fun watchEpilogue(chronicles: List<ChronicleView>)
}
