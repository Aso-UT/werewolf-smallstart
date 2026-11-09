package werewolf.phase

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration

// 処理の長さから中身を推測されないようにするため、block を実行しながら並行して duration を待ち、両方が終わるまで戻らない
fun lastingAtLeast(
    duration: Duration,
    dispatcher: CoroutineDispatcher = Dispatchers.IO,
    block: () -> Unit,
) = runBlocking {
    launch { delay(duration) }
    withContext(dispatcher) { block() }
}
