package werewolf

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.human.HumanIO
import werewolf.view.Atmosphere
import werewolf.view.ChoiceView
import werewolf.view.EpilogueMood
import werewolf.view.GameNoteView
import werewolf.view.SelectionMood

open class NothingHumanIO : HumanIO {
    override fun display(view: RecallView) { error("display not expected") }
    override fun updateGameNotePanel(view: GameNoteView) { error("updateGameNotePanel not expected") }
    override fun updateAtmosphere(atmosphere: Atmosphere) { error("updateAtmosphere not expected") }
    override fun updateSelectionMood(mood: SelectionMood) { error("updateSelectionMood not expected") }
    override fun updateEpilogueMood(mood: EpilogueMood) { error("updateEpilogueMood not expected") }
    override fun waitUntilReady() { error("waitUntilReady not expected") }
    override fun promptChoice(view: ChoiceView): String = error("promptChoice not expected")
    override fun promptFreeText(title: String, description: String): String = error("promptFreeText not expected")
    override fun promptSpeakingUrge(title: String, seconds: Int, buttons: List<String>, timeoutButton: String): String = error("promptSpeakingUrge not expected")
    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) { error("watchEpilogue not expected") }
}
