package werewolf.view

enum class PlayerStatus { ALIVE, EXECUTED, ATTACKED }

data class PlayerSummary(val name: String, val status: PlayerStatus, val claimedRole: String? = null)

data class PlayerStatusView(val players: List<PlayerSummary>)
