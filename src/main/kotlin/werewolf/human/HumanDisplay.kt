package werewolf.human

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.view.Atmosphere
import werewolf.view.EpilogueMood
import werewolf.view.GameNoteView
import werewolf.view.SelectionMood

interface HumanDisplay {
    fun display(view: RecallView)
    fun updateGameNotePanel(view: GameNoteView)
    fun updateAtmosphere(atmosphere: Atmosphere)
    fun updateSelectionMood(mood: SelectionMood)
    fun updateEpilogueMood(mood: EpilogueMood)
    fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>)
}
