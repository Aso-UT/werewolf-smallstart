package werewolf

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import werewolf.game.AllPlayers
import werewolf.game.DivineResult
import werewolf.game.GameEvent
import werewolf.game.GameSetup
import werewolf.game.MediumResult
import werewolf.game.RecallView
import werewolf.game.Role
import werewolf.game.Statement
import werewolf.human.HumanPlayer
import werewolf.phase.InitialPhase
import werewolf.view.Atmosphere
import werewolf.view.GameNoteView
import werewolf.view.PlayerNote
import werewolf.view.PlayerStatus

class GameNoteTest {

    private class CapturingIO : NothingHumanIO() {
        val panels = mutableListOf<GameNoteView>()
        override fun display(view: RecallView) {}
        override fun updateGameNotePanel(view: GameNoteView) { panels += view }
        override fun updateAtmosphere(atmosphere: Atmosphere) {}
    }

    private class SilentPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun onReceive(event: GameEvent) {}
    }

    private fun createGameWithHuman(io: CapturingIO, vararg others: Pair<String, Role>): GameSetup =
        createGameWithHumanAs(io, "V1" to Role.VILLAGER, *others)

    private fun createGameWithHumanAs(io: CapturingIO, self: Pair<String, Role>, vararg others: Pair<String, Role>): GameSetup {
        val (humanName, humanRole) = self
        val human = HumanPlayer(humanRole, humanName, io)
        val otherPlayers = others.map { (name, role) -> SilentPlayer(role, name) to role }
        return TestLodge(*otherPlayers.toTypedArray(), human to humanRole).create()
    }

    private fun noteOf(view: GameNoteView, name: String): PlayerNote = view.players.single { it.name == name }

    private fun statusOf(view: GameNoteView, name: String): PlayerStatus = noteOf(view, name).status

    private fun claimedRoleOf(view: GameNoteView, name: String): String? = noteOf(view, name).claimedRole

    private fun knownRoleOf(view: GameNoteView, name: String): String? = noteOf(view, name).knownRole

    @Test
    fun `own role is known and others are unknown at game start`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)

        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        assertEquals(Role.VILLAGER.displayName, knownRoleOf(io.panels.last(), "V1"))
        assertEquals(null, knownRoleOf(io.panels.last(), "V2"))
        assertEquals(null, knownRoleOf(io.panels.last(), "Wolf"))
    }

    @Test
    fun `werewolf ally is known to a werewolf`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHumanAs(io, "Wolf1" to Role.WEREWOLF, "Wolf2" to Role.WEREWOLF, "V1" to Role.VILLAGER)

        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        assertEquals(Role.WEREWOLF.displayName, knownRoleOf(io.panels.last(), "Wolf2"))
        assertEquals(null, knownRoleOf(io.panels.last(), "V1"))
    }

    @Test
    fun `all roles are known after roles are revealed`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        gameSetup.oracle.revealAllRoles(gameSetup.playerManager)

        assertEquals(Role.VILLAGER.displayName, knownRoleOf(io.panels.last(), "V2"))
        assertEquals(Role.WEREWOLF.displayName, knownRoleOf(io.panels.last(), "Wolf"))
    }

    @Test
    fun `all players appear as alive at game start`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)

        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        val summary = io.panels.last()
        assertTrue(summary.players.all { it.status == PlayerStatus.ALIVE })
        assertEquals(3, summary.players.size)
    }

    @Test
    fun `player is marked as executed after execution`() {
        val io = CapturingIO()
        // V2 + V3 to avoid game-over when V2 is executed (wolf would equal citizens)
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "V3" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }

        gameSetup.playerManager.execute(v2)

        assertEquals(PlayerStatus.EXECUTED, statusOf(io.panels.last(), "V2"))
        assertEquals(PlayerStatus.ALIVE, statusOf(io.panels.last(), "V1"))
    }

    @Test
    fun `player is marked as attacked after night kill`() {
        val io = CapturingIO()
        // V2 + V3 to avoid game-over when V2 is attacked (wolf would equal citizens)
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "V3" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }

        gameSetup.playerManager.kill(v2)

        assertEquals(PlayerStatus.ATTACKED, statusOf(io.panels.last(), "V2"))
    }

    @Test
    fun `panel is not updated for unrelated events`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val countAfterStart = io.panels.size

        GameEvent.DiscussionStarted.send(1, AllPlayers(gameSetup.playerManager))

        assertEquals(countAfterStart, io.panels.size)
    }

    @Test
    fun `divined result appears as own result under the divination title`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val human = gameSetup.playerManager.allPlayers.single { it.name == "V1" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }

        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, human)

        assertEquals("占い結果", io.panels.last().ownResultTitle)
        assertEquals(true, noteOf(io.panels.last(), "Wolf").ownResult)
        assertEquals(null, noteOf(io.panels.last(), "V1").ownResult)
    }

    @Test
    fun `medium result appears as own result under the medium title`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val human = gameSetup.playerManager.allPlayers.single { it.name == "V1" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }

        GameEvent.MediumRevealed.send(wolf, MediumResult.NOT_WEREWOLF, human)

        assertEquals("霊能結果", io.panels.last().ownResultTitle)
        assertEquals(false, noteOf(io.panels.last(), "Wolf").ownResult)
    }

    @Test
    fun `own result column does not appear before any result is given even for a medium`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHumanAs(io, "Me" to Role.MEDIUM, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)

        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        assertEquals(null, io.panels.last().ownResultTitle)
    }

    @Test
    fun `no reporter appears before anyone reports a result`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)

        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()

        assertEquals(emptyList(), io.panels.last().divinationReporters)
        assertEquals(emptyList(), io.panels.last().mediumReporters)
    }

    @Test
    fun `a role claim alone does not make the player a reporter`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }

        GameEvent.StatementMade.send(listOf(Statement.RoleClaim(v2, Role.MEDIUM)), AllPlayers(gameSetup.playerManager))

        assertEquals(emptyList(), io.panels.last().mediumReporters)
    }

    @Test
    fun `reported result appears under the reporter in the target's row`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }

        GameEvent.StatementMade.send(listOf(Statement.MediumReport(v2, wolf, MediumResult.WEREWOLF)), AllPlayers(gameSetup.playerManager))

        assertEquals(listOf("V2"), io.panels.last().mediumReporters)
        assertEquals(emptyList(), io.panels.last().divinationReporters)
        assertEquals(mapOf("V2" to true), noteOf(io.panels.last(), "Wolf").mediumReports)
        assertEquals(emptyMap(), noteOf(io.panels.last(), "V1").mediumReports)
        // 自分が得た結果とは混ぜない
        assertEquals(null, noteOf(io.panels.last(), "Wolf").ownResult)
        assertEquals(null, io.panels.last().ownResultTitle)
    }

    @Test
    fun `reporters are ordered by when they first revealed the role`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "V3" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val v3 = gameSetup.playerManager.allPlayers.single { it.name == "V3" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        // V3 が先に占い師だと明かしたが、結果を報告したのは V2 が先
        GameEvent.StatementMade.send(listOf(Statement.RoleClaim(v3, Role.SEER)), allPlayers)
        GameEvent.StatementMade.send(listOf(Statement.DivinationReport(v2, wolf, DivineResult.WEREWOLF)), allPlayers)
        GameEvent.StatementMade.send(listOf(Statement.DivinationReport(v3, wolf, DivineResult.NOT_WEREWOLF)), allPlayers)

        assertEquals(listOf("V3", "V2"), io.panels.last().divinationReporters)
    }

    @Test
    fun `ROLE_CLAIM statement appears as the player's claimed role`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(listOf(Statement.RoleClaim(v2, Role.SEER)), allPlayers)

        assertEquals(Role.SEER.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `DivinationReport is treated as a seer claim`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(listOf(Statement.DivinationReport(v2, wolf, DivineResult.WEREWOLF)), allPlayers)

        assertEquals(Role.SEER.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `MediumReport is treated as a medium claim`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val wolf = gameSetup.playerManager.allPlayers.single { it.name == "Wolf" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(listOf(Statement.MediumReport(v2, wolf, MediumResult.WEREWOLF)), allPlayers)

        assertEquals(Role.MEDIUM.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `later claim overrides an earlier one for the same player`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(listOf(Statement.RoleClaim(v2, Role.SEER)), allPlayers)
        GameEvent.StatementMade.send(listOf(Statement.RoleClaim(v2, Role.VILLAGER)), allPlayers)

        assertEquals(Role.VILLAGER.displayName, claimedRoleOf(io.panels.last(), "V2"))
    }

    @Test
    fun `mentioning a role in a PLAIN statement is not treated as a claim`() {
        val io = CapturingIO()
        val gameSetup = createGameWithHuman(io, "V2" to Role.VILLAGER, "Wolf" to Role.WEREWOLF)
        InitialPhase(gameSetup.playerManager, gameSetup.oracle).proceed()
        val v2 = gameSetup.playerManager.allPlayers.single { it.name == "V2" }
        val allPlayers = AllPlayers(gameSetup.playerManager)

        GameEvent.StatementMade.send(listOf(Statement.Plain(v2, "私は${Role.SEER.displayName}です")), allPlayers)

        assertEquals(null, claimedRoleOf(io.panels.last(), "V2"))
    }
}
