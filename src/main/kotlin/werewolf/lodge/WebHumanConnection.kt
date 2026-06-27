package werewolf.lodge

import io.ktor.server.application.install
import io.ktor.server.engine.EmbeddedServer
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
import werewolf.human.HumanIO
import werewolf.human.web.WebHumanIO

class WebHumanConnection : HumanConnection {
    private lateinit var webHumanIO: WebHumanIO
    private lateinit var server: EmbeddedServer<*, *>

    override fun createIO(): HumanIO {
        webHumanIO = WebHumanIO()
        return webHumanIO
    }

    override fun setup() {
        val observer = ConnectionObserver()
        server = createServer(observer)
        server.start(wait = false)
        println("http://localhost:$PORT をブラウザで開いてください")
        observer.waitForConnectionEstablished()
        println("接続を確認しました。ゲームを開始します。")
    }

    override fun teardown() {
        Thread.sleep(EPILOGUE_DRAIN_DELAY_MS)
        server.stop(SERVER_STOP_GRACE_MS, SERVER_STOP_TIMEOUT_MS)
    }

    private fun createServer(observer: ConnectionObserver): EmbeddedServer<*, *> =
        embeddedServer(Netty, port = PORT) {
            install(WebSockets)
            routing {
                staticResources("/", "static/svelte") { default("index.html") }
                webSocket("/game") {
                    observer.notifyConnection()
                    relayToPlayer()
                    relayToClient(this)
                }
            }
        }

    private fun DefaultWebSocketSession.relayToPlayer() {
        launch {
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    if (text == "abort") {
                        webHumanIO.requestAbort()
                        webHumanIO.incoming.trySend("")
                    } else {
                        webHumanIO.incoming.trySend(text)
                    }
                }
            }
        }
    }

    private suspend fun relayToClient(session: DefaultWebSocketSession) {
        for (message in webHumanIO.outgoing) session.send(message)
    }

    companion object {
        private const val PORT = 8080
        private const val EPILOGUE_DRAIN_DELAY_MS = 2000L
        private const val SERVER_STOP_GRACE_MS = 100L
        private const val SERVER_STOP_TIMEOUT_MS = 1000L
    }
}

private class ConnectionObserver {
    private val latch = CountDownLatch(1)
    fun notifyConnection() = latch.countDown()
    fun waitForConnectionEstablished() = latch.await()
}
