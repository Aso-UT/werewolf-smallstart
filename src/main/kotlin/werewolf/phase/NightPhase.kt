package werewolf.phase

import werewolf.game.AllPlayers
import werewolf.game.GameEvent
import werewolf.game.MajorityVoteResolver
import werewolf.game.NightAction
import werewolf.game.Oracle
import werewolf.game.Player
import werewolf.game.PlayerManager
import werewolf.game.TimeOfDay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class NightPhase(
    private val playerManager: PlayerManager,
    private val oracle: Oracle,
    private val nightNumber: Int,
    private val conclaveTime: Duration = CONCLAVE_TIME,
) : Phase {
    override fun proceed(): Phase {
        GameEvent.TimeChanged.send(TimeOfDay.Night(nightNumber), AllPlayers(playerManager))
        lastingAtLeast(conclaveTime) { Conclave(oracle, playerManager, nightNumber).conduct() }
        val decisions = playerManager.players.askInParallel { it.buildNightAction(playerManager.players, nightNumber == 1) }

        val attacks = decisions.values.filterIsInstance<NightAction.Attack>()
        val guards = decisions.values.filterIsInstance<NightAction.Guard>()
        attackIfNotGuarded(attacks, guards)

        revealNightSecrets(decisions)
        return MorningPhase(playerManager, oracle, nightNumber)
    }

    private fun attackIfNotGuarded(attacks: List<NightAction.Attack>, guards: List<NightAction.Guard>) {
        val target = MajorityVoteResolver.resolve(attacks.map { it.target }) ?: return
        if (guards.none { it.target === target }) playerManager.kill(target)
    }

    private fun revealNightSecrets(decisions: Map<Player, NightAction>) {
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

    companion object {
        // 密談がない夜（人狼が1人以下）も同じだけ待ち、夜の長さから密談の有無＝残りの人狼の数を推測されないようにする。
        // 人狼2人の密談（AIの発言が最大6回）がふつうに収まる長さとして仮に決めた値で、#305 の計測結果が出たら見直す
        private val CONCLAVE_TIME = 60.seconds
    }
}
