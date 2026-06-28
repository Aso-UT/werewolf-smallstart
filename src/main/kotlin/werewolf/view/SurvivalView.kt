package werewolf.view

enum class PlayerStatus { ALIVE, EXECUTED, ATTACKED }

data class SurvivalView(val players: Map<String, PlayerStatus>)
