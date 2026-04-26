package org.example

class RoleAwareCpuPlayer(private val myRole: Role, override val name: String) : Player(myRole) {
    private val divinedResults = mutableMapOf<Player, DivineResult>()
    private val unspokenDivinations = mutableListOf<GameEvent.Divined>()

    override fun onReceive(event: GameEvent) {
        if (event is GameEvent.Divined) {
            divinedResults[event.target] = event.result
            unspokenDivinations.add(event)
        }
    }

    override fun discuss(players: List<Player>): String {
        if (myRole != Role.SEER || unspokenDivinations.isEmpty()) return ""
        return unspokenDivinations.removeFirst().body(this)
    }

    override fun selectTarget(context: SelectionContext): Player = when {
        myRole == Role.SEER && context is SelectionContext.Divine -> selectDivineTarget(context)
        myRole == Role.SEER && context is SelectionContext.Vote   -> selectVoteTarget(context)
        else -> context.candidates().random()
    }

    private fun selectDivineTarget(context: SelectionContext.Divine): Player {
        val candidates = context.candidates()
        val undivined = candidates.filter { it !in divinedResults }
        return if (undivined.isNotEmpty()) undivined.random() else candidates.random()
    }

    private fun selectVoteTarget(context: SelectionContext.Vote): Player {
        val candidates = context.candidates()
        val confirmedWerewolves = candidates.filter { divinedResults[it] == DivineResult.WEREWOLF }
        if (confirmedWerewolves.isNotEmpty()) return confirmedWerewolves.random()
        val confirmedVillagers = candidates.filter { divinedResults[it] == DivineResult.NOT_WEREWOLF }
        val votable = candidates - confirmedVillagers.toSet()
        return if (votable.isNotEmpty()) votable.random() else candidates.random()
    }
}
