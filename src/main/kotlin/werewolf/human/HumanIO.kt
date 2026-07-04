package werewolf.human

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.view.ChoiceView
import werewolf.view.DivinationView
import werewolf.view.PlayerStatusView

interface HumanIO {
    fun display(view: RecallView)
    fun updatePlayerStatusPanel(view: PlayerStatusView)
    fun updateDivinationPanel(view: DivinationView)
    fun promptChoice(view: ChoiceView): String
    fun promptFreeText(title: String, description: String): String
    fun watchEpilogue(chronicles: List<ChronicleView>)
}
