package werewolf

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.human.HumanIO
import werewolf.view.Atmosphere
import werewolf.view.ChoiceView
import werewolf.view.DivinationView
import werewolf.view.PlayerStatusView

open class NothingHumanIO : HumanIO {
    override fun display(view: RecallView) { error("display not expected") }
    override fun updatePlayerStatusPanel(view: PlayerStatusView) { error("updatePlayerStatusPanel not expected") }
    override fun updateDivinationPanel(view: DivinationView) { error("updateDivinationPanel not expected") }
    override fun updateAtmosphere(atmosphere: Atmosphere) { error("updateAtmosphere not expected") }
    override fun promptChoice(view: ChoiceView): String = error("promptChoice not expected")
    override fun promptFreeText(title: String, description: String): String = error("promptFreeText not expected")
    override fun watchEpilogue(chronicles: List<ChronicleView>) { error("watchEpilogue not expected") }
}
