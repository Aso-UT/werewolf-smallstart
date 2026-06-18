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
import werewolf.ai.AiPersonalities
import werewolf.ai.AiPlayer
import werewolf.ai.Instruction
import werewolf.ai.anthropic.AnthropicLanguageModel
import werewolf.game.Player
import werewolf.game.Role
import werewolf.web.WebPlayer

class WebLodge : Lodge() {
    private val aiNames = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi")
    private lateinit var server: EmbeddedServer<*, *>

    lateinit var webPlayer: WebPlayer
        private set

    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        webPlayer = WebPlayer(roles[0], "Ivan")
        val personalities = AiPersonalities.list.shuffled()
        val aiPlayers: List<Pair<Player, Role>> = roles.drop(1).mapIndexed { i, role ->
            AiPlayer(role, aiNames[i], AnthropicLanguageModel(HAIKU_MODEL), Instruction(aiNames[i], personalities[i])) to role
        }
        return listOf(webPlayer to roles[0]) + aiPlayers
    }

    override fun setup() {
        val clientConnected = CountDownLatch(1)
        server = embeddedServer(Netty, port = PORT) {
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
        }
        server.start(wait = false)
        println("http://localhost:$PORT をブラウザで開いてください")
        clientConnected.await()
        println("接続を確認しました。ゲームを開始します。")
    }

    override fun teardown() {
        Thread.sleep(EPILOGUE_DRAIN_DELAY_MS)
        server.stop(SERVER_STOP_GRACE_MS, SERVER_STOP_TIMEOUT_MS)
    }

    private fun DefaultWebSocketSession.receiveAbortRequests(webPlayer: WebPlayer) {
        launch {
            for (frame in incoming) {
                if (frame is Frame.Text && frame.readText() == "abort") {
                    webPlayer.requestAbort()
                }
            }
        }
    }

    companion object {
        private const val PORT = 8080
        private const val EPILOGUE_DRAIN_DELAY_MS = 2000L
        private const val SERVER_STOP_GRACE_MS = 100L
        private const val SERVER_STOP_TIMEOUT_MS = 1000L
        private const val HAIKU_MODEL = "claude-haiku-4-5-20251001"
    }
}
