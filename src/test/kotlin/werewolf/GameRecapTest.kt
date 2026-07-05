package werewolf

import werewolf.game.*
import werewolf.phase.Conclave
import werewolf.phase.OpenDiscussion

import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameRecapTest {

    private class SpeakingPlayer(role: Role, name: String) : ReceivingPlayer(role, name) {
        override fun speak(context: DiscussionContext): Claim = Claim(this, context, Statement.Plain("$name speaks"), "intent")
    }

    private fun playerManager(vararg players: Player): PlayerManager =
        TestLodge(*players.map { it to Role.VILLAGER }.toTypedArray()).create().playerManager

    private fun anySignal(): GameOverSignal {
        return try {
            GameOverSignal.throwIfGameOver(AliveCounts(mapOf(Side.CITIZEN to 0, Side.WEREWOLF to 1)))
            error("unreachable")
        } catch (s: GameOverSignal) { s }
    }

    @Test
    fun `public events appear only once even when received by multiple players`() {
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val pm = playerManager(v1, v2)

        GameEvent.TimeChanged.send(TimeOfDay.Night(1), AllPlayers(pm))

        val chronicles = GameRecap(pm, anySignal()).chronicles()
        assertEquals(1, chronicles.size)
    }

    @Test
    fun `private events from each player are all collected`() {
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val pm = playerManager(v1, v2)

        GameEvent.RoleAssigned.send(Role.VILLAGER, v1)
        GameEvent.RoleAssigned.send(Role.VILLAGER, v2)

        val chronicles = GameRecap(pm, anySignal()).chronicles()
        assertEquals(2, chronicles.size)
    }

    @Test
    fun `chronicles are sorted by creation order across players`() {
        val v1 = ReceivingPlayer(Role.VILLAGER, "V1")
        val v2 = ReceivingPlayer(Role.VILLAGER, "V2")
        val pm = playerManager(v1, v2)

        // 作成順: RoleAssigned(A) → RoleAssigned(B) → TimeChanged(全員)
        // flatMap後: [RoleAssigned(A), TimeChanged, RoleAssigned(B), TimeChanged]
        // distinct後: [RoleAssigned(A), TimeChanged, RoleAssigned(B)] ← ソートなしでは誤順
        // sortedBy後: [RoleAssigned(A), RoleAssigned(B), TimeChanged] ← 正しい順
        GameEvent.RoleAssigned.send(Role.VILLAGER, v1)
        GameEvent.RoleAssigned.send(Role.VILLAGER, v2)
        GameEvent.TimeChanged.send(TimeOfDay.Night(1), AllPlayers(pm))

        val chronicles = GameRecap(pm, anySignal()).chronicles()
        assertEquals(3, chronicles.size)
        val v1Chronicle = assertIs<ChronicleView.Observation>(chronicles[0])
        assertEquals("V1", v1Chronicle.recipient)
        val v2Chronicle = assertIs<ChronicleView.Observation>(chronicles[1])
        assertEquals("V2", v2Chronicle.recipient)
        val timeChronicle = assertIs<ChronicleView.Observation>(chronicles[2])
        assertEquals("全プレイヤー", timeChronicle.recipient)
    }

    @Test
    fun `chronicles do not throw after open discussion produces StatementMade`() {
        val v1 = SpeakingPlayer(Role.VILLAGER, "V1")
        val v2 = SpeakingPlayer(Role.VILLAGER, "V2")
        val pm = playerManager(v1, v2)

        OpenDiscussion(pm, day = 1).conduct()

        val chronicles = GameRecap(pm, anySignal()).chronicles()
        assertTrue(chronicles.any { it is ChronicleView.Action && it.category == "議論" })
    }

    @Test
    fun `chronicles do not throw after conclave produces WerewolfStatementMade`() {
        val w1 = SpeakingPlayer(Role.WEREWOLF, "W1")
        val w2 = SpeakingPlayer(Role.WEREWOLF, "W2")
        val setup = TestLodge(w1 to Role.WEREWOLF, w2 to Role.WEREWOLF).create()

        Conclave(setup.oracle, setup.playerManager, day = 1).conduct()

        val chronicles = GameRecap(setup.playerManager, anySignal()).chronicles()
        assertTrue(chronicles.any { it is ChronicleView.Action && it.category == "密談" })
    }
}