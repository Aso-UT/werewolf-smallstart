package werewolf

import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.DefaultWebSocketSession
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import io.ktor.websocket.send
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.launch
import werewolf.game.GameOverSignal
import werewolf.lodge.WebLodge
import werewolf.phase.Epilogue
import werewolf.phase.InitialPhase
import werewolf.phase.Phase
import werewolf.web.WebPlayer

private const val PORT = 8080
private const val EPILOGUE_DRAIN_DELAY_MS = 2000L
private const val SERVER_STOP_GRACE_MS = 100L
private const val SERVER_STOP_TIMEOUT_MS = 1000L

fun main() {
    val lodge = WebLodge()
    val setup = lodge.create()
    val webPlayer = lodge.webPlayer
    val clientConnected = CountDownLatch(1)

    val server = embeddedServer(Netty, port = PORT) {
        install(WebSockets)
        routing {
            staticResources("/", "static") {
                default("index.html")
            }
            webSocket("/game") {
                clientConnected.countDown()
                receiveAbortRequests(webPlayer)
                for (message in webPlayer.outgoing) {
                    send(message)
                }
            }
        }
    }.start(wait = false)

    println("http://localhost:$PORT をブラウザで開いてください")
    clientConnected.await()
    println("接続を確認しました。ゲームを開始します。")

    try {
        var phase: Phase = InitialPhase(setup.playerManager, setup.oracle)
        while (true) { phase = phase.proceed() }
    } catch (signal: GameOverSignal) {
        Epilogue(setup.playerManager, setup.oracle, signal).perform()
    }

    Thread.sleep(EPILOGUE_DRAIN_DELAY_MS)
    server.stop(SERVER_STOP_GRACE_MS, SERVER_STOP_TIMEOUT_MS)
}

private suspend fun DefaultWebSocketSession.receiveAbortRequests(webPlayer: WebPlayer) {
    launch {
        for (frame in incoming) {
            if (frame is Frame.Text && frame.readText() == "abort") {
                webPlayer.requestAbort()
            }
        }
    }
}
