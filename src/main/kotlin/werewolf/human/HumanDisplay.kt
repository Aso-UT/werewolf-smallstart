package werewolf.human

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.view.Atmosphere
import werewolf.view.DivinationView
import werewolf.view.EpilogueMood
import werewolf.view.PlayerStatusView
import werewolf.view.SelectionMood

interface HumanDisplay {
    fun display(view: RecallView)
    fun updatePlayerStatusPanel(view: PlayerStatusView)
    fun updateDivinationPanel(view: DivinationView)
    fun updateAtmosphere(atmosphere: Atmosphere)
    fun updateSelectionMood(mood: SelectionMood)
    fun updateEpilogueMood(mood: EpilogueMood)
    fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>)
}
