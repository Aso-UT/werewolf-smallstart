package werewolf.human

import werewolf.game.GameEvent
import werewolf.game.SelectionContext
import werewolf.game.Side
import werewolf.game.TimeOfDay
import werewolf.view.Atmosphere
import werewolf.view.EpilogueMood
import werewolf.view.GameNoteView
import werewolf.view.SelectionMood

class HumanScreen(private val io: HumanDisplay) {
    private val gameNote = GameNote()
    private var lastGameNote: GameNoteView? = null

    fun show(event: GameEvent) {
        io.display(event.toRecallView())
        gameNote.post(event)
        refreshGameNotePanel()
        atmosphereOf(event)?.let { io.updateAtmosphere(it) }
        epilogueMoodOf(event)?.let { io.updateEpilogueMood(it) }
    }

    fun showSelectionMood(context: SelectionContext) {
        io.updateSelectionMood(moodOf(context))
    }

    private fun refreshGameNotePanel() {
        val current = gameNote.summary()
        if (current == lastGameNote) return
        lastGameNote = current
        io.updateGameNotePanel(current)
    }

    private fun atmosphereOf(event: GameEvent): Atmosphere? = when (event) {
        is GameEvent.TimeChanged -> when (event.timeOfDay) {
            TimeOfDay.Morning -> Atmosphere.MORNING
            is TimeOfDay.Night -> Atmosphere.NIGHT
        }
        is GameEvent.DiscussionStarted -> Atmosphere.DAY
        is GameEvent.VoteStarted -> Atmosphere.VOTE
        else -> null
    }

    private fun epilogueMoodOf(event: GameEvent): EpilogueMood? {
        if (event !is GameEvent.GameResult) return null
        return when (event.winnerSide) {
            Side.CITIZEN -> if (event.isWinner) EpilogueMood.CITIZEN_WIN else EpilogueMood.CITIZEN_LOSE
            Side.WEREWOLF -> if (event.isWinner) EpilogueMood.WEREWOLF_WIN else EpilogueMood.WEREWOLF_LOSE
        }
    }

    private fun moodOf(context: SelectionContext): SelectionMood = when (context) {
        is SelectionContext.Attack -> SelectionMood.ATTACK
        is SelectionContext.Divine -> SelectionMood.DIVINE
        is SelectionContext.Guard -> SelectionMood.GUARD
        is SelectionContext.Vote -> SelectionMood.VOTE
    }
}
