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
        private val eagernessOf: (DiscussionContext) -> Eagerness,
        private val receivesExecutionEvent: Boolean = false,
    ) : NothingPlayer(role, name) {
        private val _log = mutableListOf<String>()
        val log: List<String> get() = _log
        private val _rounds = mutableListOf<Int>()
        val rounds: List<Int> get() = _rounds
        var receivedStartedDay: Int? = null
        var urgeCount = 0
            private set
        private var statementIndex = 0

        override fun feelUrge(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): SpeakingUrge {
            urgeCount++
            return SpeakingUrge(this, context, eagernessOf(context), "", UnimplementedActionMetadata)
        }

        override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
            val statement = statements[statementIndex++]
            _log.add("$name:said:$statement")
            _rounds.add(context.round)
            return Claim(this, context, Statement.Plain(this, statement), claimableRoles, reportEligibility, "", UnimplementedActionMetadata)
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

    // 発言するほど意欲が下がり、同じ発言回数なら priority が小さい方が意欲が高い。
    // priority を 0, 1, 2... と割り振ると、その順に1回ずつ発言が回る
    private fun takingTurns(priority: Int): (DiscussionContext) -> Eagerness =
        { context -> Eagerness(-(context.round * TURN_STEP + priority)) }

    private fun constant(value: Int): (DiscussionContext) -> Eagerness = { Eagerness(value) }

    @Test
    fun `all players receive discussion started notification`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("a", "b", "c"), takingTurns(0))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("d", "e", "f"), takingTurns(1))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

        assertEquals(1, alice.receivedStartedDay)
        assertEquals(1, bob.receivedStartedDay)
    }

    @Test
    fun `statements are delivered immediately in order`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf(
            "私はBobが怪しいと思う",
            "やはりBobだと思う",
            "Bobに投票します"
        ), takingTurns(0))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf(
            "私は無実です",
            "Aliceこそ怪しい",
            "Aliceに投票します"
        ), takingTurns(1))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

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
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("村人として発言します", "続けて発言します", "最終発言です"), takingTurns(0), receivesExecutionEvent = true)
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("人狼として発言します", "続けて発言します", "最終発言です"), takingTurns(1), receivesExecutionEvent = true)
        val charlie = ScriptedPlayer(Role.VILLAGER, "Charlie", listOf("同じく疑っています", "まだ様子見です", "最終判断します"), takingTurns(2), receivesExecutionEvent = true)
        val victim = ScriptedPlayer(Role.VILLAGER, "Victim", emptyList(), constant(Int.MAX_VALUE), receivesExecutionEvent = true)
        val playerManager = TestLodge(
            alice to Role.VILLAGER, bob to Role.WEREWOLF, charlie to Role.VILLAGER, victim to Role.VILLAGER
        ).create().playerManager
        playerManager.execute(victim)

        OpenDiscussion(playerManager, 1).conduct()

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

    @Test
    fun `player with highest eagerness speaks`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("1", "2", "3"), constant(0))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("1", "2", "3"), constant(1))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

        assertEquals("Bob", speakingOrderHeardBy(alice).first())
    }

    @Test
    fun `the same player does not speak twice in a row`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("1", "2", "3"), constant(1))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("1", "2", "3"), constant(0))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

        assertEquals(listOf("Alice", "Bob", "Alice", "Bob", "Alice", "Bob"), speakingOrderHeardBy(alice))
    }

    @Test
    fun `eagerness is not asked when only one player can speak`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("1", "2", "3"), constant(1))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("1", "2", "3"), constant(0))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

        // 2人では、最初の1回以外は直前の発言者でない方しか発言できない
        assertEquals(1, alice.urgeCount)
        assertEquals(1, bob.urgeCount)
    }

    @Test
    fun `player who reached the statement limit no longer speaks`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("1", "2", "3", "4"), constant(2))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("1", "2", "3", "4"), constant(1))
        val charlie = ScriptedPlayer(Role.VILLAGER, "Charlie", listOf("1"), constant(0))
        val playerManager = TestLodge(
            alice to Role.VILLAGER, bob to Role.WEREWOLF, charlie to Role.VILLAGER
        ).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

        // 4回発言した Alice・Bob は意欲が高くても選ばれず、Charlie に回る
        assertEquals(
            listOf("Alice", "Bob", "Alice", "Bob", "Alice", "Bob", "Alice", "Bob", "Charlie"),
            speakingOrderHeardBy(alice),
        )
    }

    @Test
    fun `discussion ends when total statements reach three per speaker`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("1", "2", "3", "4"), takingTurns(0))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("1", "2", "3", "4"), takingTurns(1))
        val charlie = ScriptedPlayer(Role.VILLAGER, "Charlie", listOf("1", "2", "3", "4"), takingTurns(2))
        val playerManager = TestLodge(
            alice to Role.VILLAGER, bob to Role.WEREWOLF, charlie to Role.VILLAGER
        ).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

        // 全員3回ずつ発言した時点では、まだ誰も上限（4回）に達していないが打ち切られる
        assertEquals(9, speakingOrderHeardBy(alice).size)
    }

    @Test
    fun `round is the count of the speaker's own statements`() {
        val alice = ScriptedPlayer(Role.VILLAGER, "Alice", listOf("1", "2", "3"), constant(1))
        val bob = ScriptedPlayer(Role.WEREWOLF, "Bob", listOf("1", "2", "3"), constant(0))
        val playerManager = TestLodge(alice to Role.VILLAGER, bob to Role.WEREWOLF).create().playerManager

        OpenDiscussion(playerManager, 1).conduct()

        assertEquals(listOf(1, 2, 3), alice.rounds)
        assertEquals(listOf(1, 2, 3), bob.rounds)
    }

    private fun speakingOrderHeardBy(listener: ScriptedPlayer): List<String> =
        listener.log.filter { it.contains(":heard:") }.map { it.split(":")[2] }

    companion object {
        private const val TURN_STEP = 10
    }
}
