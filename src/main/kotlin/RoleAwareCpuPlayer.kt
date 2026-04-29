package org.example

class RoleAwareCpuPlayer(private val myRole: Role, override val name: String) : Player(myRole) {
    private val myDivinedResults = mutableMapOf<Player, DivineResult>()
    private val reportedDivinations = mutableMapOf<Player, DivineResult>()
    private val unspokenDivinations = mutableListOf<GameEvent.Divined>()
    private val unspokenMediumReveals = mutableListOf<GameEvent.MediumRevealed>()
    private val claimedSeers = mutableSetOf<Player>()

    override fun onReceive(event: GameEvent) {
        when (event) {
            is GameEvent.Divined -> {
                myDivinedResults[event.target] = event.result
                unspokenDivinations.add(event)
            }
            is GameEvent.MediumRevealed -> unspokenMediumReveals.add(event)
            is GameEvent.StatementMade -> {
                val statement = event.statement
                if (statement is Statement.DivinationReport && statement.claimant !== this) {
                    reportedDivinations[statement.target] = statement.result
                    claimedSeers.add(statement.claimant)
                }
            }
            else -> {}
        }
    }

    override fun discuss(players: List<Player>): Statement {
        if (myRole == Role.SEER && unspokenDivinations.isNotEmpty()) {
            val event = unspokenDivinations.removeFirst()
            return Statement.DivinationReport(claimant = this, target = event.target, result = event.result)
        }
        if (myRole == Role.MEDIUM && unspokenMediumReveals.isNotEmpty()) {
            val event = unspokenMediumReveals.removeFirst()
            return Statement.MediumReport(claimant = this, target = event.target, result = event.result)
        }
        return Statement.Plain("")
    }

    override fun selectTarget(context: SelectionContext): Player = when {
        myRole == Role.SEER && context is SelectionContext.Divine -> selectDivineTarget(context)
        context is SelectionContext.Vote -> selectVoteTarget(context)
        context is SelectionContext.Attack -> selectAttackTarget(context)
        context is SelectionContext.Guard -> selectGuardTarget(context)
        else -> context.candidates().random()
    }

    private fun selectDivineTarget(context: SelectionContext.Divine): Player {
        val candidates = context.candidates()
        val undivined = candidates.filter { it !in myDivinedResults }
        return if (undivined.isNotEmpty()) undivined.random() else candidates.random()
    }

    private fun selectAttackTarget(context: SelectionContext.Attack): Player {
        val candidates = context.candidates()
        val targetSeers = candidates.filter { it in claimedSeers }
        return if (targetSeers.isNotEmpty()) targetSeers.random() else candidates.random()
    }

    private fun selectGuardTarget(context: SelectionContext.Guard): Player {
        val candidates = context.candidates()
        val targetSeers = candidates.filter { it in claimedSeers }
        return if (targetSeers.isNotEmpty()) targetSeers.random() else candidates.random()
    }

    private fun selectVoteTarget(context: SelectionContext.Vote): Player {
        val candidates = context.candidates()
        // ① 自分の占いで人狼と確認した候補
        val myConfirmedWerewolves = candidates.filter { myDivinedResults[it] == DivineResult.WEREWOLF }
        if (myConfirmedWerewolves.isNotEmpty()) return myConfirmedWerewolves.random()
        // ② 他者が人狼と報告した候補（自分の占いで非人狼と確認した者は除く）
        val reportedWerewolves = candidates.filter {
            reportedDivinations[it] == DivineResult.WEREWOLF && myDivinedResults[it] != DivineResult.NOT_WEREWOLF
        }
        if (reportedWerewolves.isNotEmpty()) return reportedWerewolves.random()
        // ③ 自分の占いまたは他者の報告で非人狼と確認した候補を除いてランダム
        val confirmedNotWerewolf = candidates.filter {
            myDivinedResults[it] == DivineResult.NOT_WEREWOLF || reportedDivinations[it] == DivineResult.NOT_WEREWOLF
        }
        val votable = candidates - confirmedNotWerewolf.toSet()
        return if (votable.isNotEmpty()) votable.random() else candidates.random()
    }
}
