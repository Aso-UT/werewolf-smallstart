package werewolf.phase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import werewolf.game.Player

// 他のプレイヤーの答えを参照しない問い合わせを、全員に並列で行う。AIの応答待ちが人数分積み重ならないようにするため
fun <T> Collection<Player>.askInParallel(
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ask: (Player) -> T,
): Map<Player, T> = runBlocking {
    map { player -> async(dispatcher) { player to ask(player) } }.awaitAll().toMap()
}
