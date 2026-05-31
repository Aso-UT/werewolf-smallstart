package werewolf

import werewolf.game.*
import werewolf.phase.*

import kotlin.test.Test
import kotlin.test.assertEquals

class DiscussionTest {

    private class ScriptedPlayer(
        role: Role,
        name: String,
        private val statements: List<String>,
        private val receivesExecutionEvent: Boolean = false,
    ) : NothingPlayer(role, name) {
        private val _log = mutableListOf<String>()
        val log: List<String> get() = _log
        var receivedStartedDay: Int? = null
        private var statementIndex = 0

        override fun speak(context: DiscussionContext): Claim {
            val statement = statements[statementIndex++]
            _log.add("$name:said:$statement")
            return Claim(this, context, Statement.Plain(statement), "")
        }

        override fun onReceive(event: GameEvent) {
            when {
                event is GameEvent.DiscussionStarted -> receivedStartedDay = event.day
                event is GameEvent.StatementMade -> _log.add("$name:heard:${event.speakerName}:${event.statement.text()}")
                receivesExecutionEvent && event is GameEvent.PlayerExecuted -> {}
                else -> error("unexpected event in discussion test: $event")
            }
        }
    }

    private fun fixedOrderDiscussion(playerManager: PlayerManager) =
        object : OpenDiscussion(playerManager, 1) {
            override fun speakingOrder(speakers: List<Player>) = speakers
        }

    @Test
    fun `all players receive discussion started notification`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("a", "b", "c"))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("d", "e", "f"))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        fixedOrderDiscussion(playerManager).conduct()

        assertEquals(1, alice.receivedStartedDay)
        assertEquals(1, bob.receivedStartedDay)
    }

    @Test
    fun `statements are delivered immediately in order`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf(
            "私はBobが怪しいと思う",
            "やはりBobだと思う",
            "Bobに投票します"
        ))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf(
            "私は無実です",
            "Aliceこそ怪しい",
            "Aliceに投票します"
        ))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        fixedOrderDiscussion(playerManager).conduct()

        assertEquals(listOf(
            "Alice:said:私はBobが怪しいと思う",
            "Alice:heard:Alice:私はBobが怪しいと思う",
            "Alice:heard:Bob:私は無実です",
            "Alice:said:やはりBobだと思う",
            "Alice:heard:Alice:やはりBobだと思う",
            "Alice:heard:Bob:Aliceこそ怪しい",
            "Alice:said:Bobに投票します",
            "Alice:heard:Alice:Bobに投票します",
            "Alice:heard:Bob:Aliceに投票します",
        ), alice.log)

        assertEquals(listOf(
            "Bob:heard:Alice:私はBobが怪しいと思う",
            "Bob:said:私は無実です",
            "Bob:heard:Bob:私は無実です",
            "Bob:heard:Alice:やはりBobだと思う",
            "Bob:said:Aliceこそ怪しい",
            "Bob:heard:Bob:Aliceこそ怪しい",
            "Bob:heard:Alice:Bobに投票します",
            "Bob:said:Aliceに投票します",
            "Bob:heard:Bob:Aliceに投票します",
        ), bob.log)
    }

    @Test
    fun `dead players receive all statements without speaking`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("村人として発言します", "続けて発言します", "最終発言です"), receivesExecutionEvent = true)
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("人狼として発言します", "続けて発言します", "最終発言です"), receivesExecutionEvent = true)
        val charlie = ScriptedPlayer(Role.VILLAGER, "Charlie", listOf("同じく疑っています", "まだ様子見です", "最終判断します"), receivesExecutionEvent = true)
        val victim = ScriptedPlayer(Role.VILLAGER, "Victim", emptyList(), receivesExecutionEvent = true)
        val playerManager = TestLodge(
            alice to Role.VILLAGER, bob to Role.WEREWOLF, charlie to Role.VILLAGER, victim to Role.VILLAGER
        ).create().playerManager
        playerManager.execute(victim)

        fixedOrderDiscussion(playerManager).conduct()

        assertEquals(listOf(
            "Victim:heard:Alice:村人として発言します",
            "Victim:heard:Bob:人狼として発言します",
            "Victim:heard:Charlie:同じく疑っています",
            "Victim:heard:Alice:続けて発言します",
            "Victim:heard:Bob:続けて発言します",
            "Victim:heard:Charlie:まだ様子見です",
            "Victim:heard:Alice:最終発言です",
            "Victim:heard:Bob:最終発言です",
            "Victim:heard:Charlie:最終判断します",
        ), victim.log)
    }
}
