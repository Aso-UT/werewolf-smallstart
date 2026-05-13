package werewolf.game

sealed interface NightAction {
    data object None : NightAction
    data class Attack(val target: Player) : NightAction
    data object FirstNightDivine : NightAction
    data class Divine(val target: Player) : NightAction
    data class MediumReveal(val target: Player) : NightAction
    data class Guard(val target: Player) : NightAction
}
