package org.example

sealed interface NightAction {
    data object None : NightAction
    data class Attack(val target: Player) : NightAction
    data class Divine(val target: Player) : NightAction
    data object MediumReveal : NightAction
    data class Guard(val target: Player) : NightAction
}