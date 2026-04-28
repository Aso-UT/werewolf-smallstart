package org.example

class NightPhase(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val nightNumber: Int
) : Phase {
    override fun proceed(): Phase {
        GameEvent.TimeChanged.send(TimeOfDay.Night(nightNumber), playerManager.allPlayers)
        Conclave(oracle, playerManager).conduct()
        val decisions = playerManager.players.map { it to it.buildNightAction(playerManager.players, nightNumber == 1) }

        val attacks = decisions.map { it.second }.filterIsInstance<NightAction.Attack>()
        val guards = decisions.map { it.second }.filterIsInstance<NightAction.Guard>()
        attackIfNotGuarded(attacks, guards)

        revealNightSecrets(decisions)
        return MorningPhase(playerManager, oracle, nightNumber)
    }

    private fun attackIfNotGuarded(attacks: List<NightAction.Attack>, guards: List<NightAction.Guard>) {
        val target = MajorityVoteResolver.resolve(attacks.map { it.target }) ?: return
        if (guards.none { it.target === target }) playerManager.kill(target)
    }

    private fun revealNightSecrets(decisions: List<Pair<Player, NightAction>>) {
        decisions.forEach { (player, decision) ->
            when (decision) {
                is NightAction.None -> Unit
                is NightAction.Attack -> Unit
                is NightAction.Guard -> Unit
                is NightAction.FirstNightDivine -> oracle.firstNightDivine(player, playerManager.players)
                is NightAction.Divine -> oracle.divine(player, decision.target)
                is NightAction.MediumReveal -> oracle.mediumReveal(player, decision.target)
            }
        }
    }
}
