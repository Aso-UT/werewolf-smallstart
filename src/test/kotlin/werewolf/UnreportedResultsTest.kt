package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertEquals

class UnreportedResultsTest {

    private fun memoriesFor(player: Player): List<Recallable> = player.reveal(fakeCitizenWinSignal())

    @Test
    fun `divinations returns all divined targets when none reported yet`() {
        val self = ReceivingPlayer(Role.SEER, "Self")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val villager = ReceivingPlayer(Role.VILLAGER, "Villager")
        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, self)
        GameEvent.Divined.send(villager, DivineResult.NOT_WEREWOLF, self)

        val result = UnreportedResults(memoriesFor(self), self)

        assertEquals(mapOf<Player, DivineResult>(wolf to DivineResult.WEREWOLF, villager to DivineResult.NOT_WEREWOLF), result.divinations)
    }

    @Test
    fun `divinations excludes targets already reported by self`() {
        val self = ReceivingPlayer(Role.SEER, "Self")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val allPlayers = AllPlayers(TestLodge(self to Role.SEER, wolf to Role.WEREWOLF).create().playerManager)
        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, self)
        GameEvent.StatementMade.send(1, self.name, Statement.DivinationReport(self, wolf, DivineResult.WEREWOLF), allPlayers)

        val result = UnreportedResults(memoriesFor(self), self)

        assertEquals(emptyMap(), result.divinations)
    }

    @Test
    fun `divinations ignores divination reports made by other players`() {
        val self = ReceivingPlayer(Role.SEER, "Self")
        val madman = ReceivingPlayer(Role.MADMAN, "Madman")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val allPlayers = AllPlayers(
            TestLodge(self to Role.SEER, madman to Role.MADMAN, wolf to Role.WEREWOLF).create().playerManager
        )
        GameEvent.Divined.send(wolf, DivineResult.WEREWOLF, self)
        GameEvent.StatementMade.send(1, madman.name, Statement.DivinationReport(madman, wolf, DivineResult.WEREWOLF), allPlayers)

        val result = UnreportedResults(memoriesFor(self), self)

        assertEquals(mapOf<Player, DivineResult>(wolf to DivineResult.WEREWOLF), result.divinations)
    }

    @Test
    fun `mediums returns all revealed targets when none reported yet`() {
        val self = ReceivingPlayer(Role.MEDIUM, "Self")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        GameEvent.MediumRevealed.send(wolf, MediumResult.WEREWOLF, self)

        val result = UnreportedResults(memoriesFor(self), self)

        assertEquals(mapOf<Player, MediumResult>(wolf to MediumResult.WEREWOLF), result.mediums)
    }

    @Test
    fun `mediums excludes targets already reported by self`() {
        val self = ReceivingPlayer(Role.MEDIUM, "Self")
        val wolf = ReceivingPlayer(Role.WEREWOLF, "Wolf")
        val allPlayers = AllPlayers(TestLodge(self to Role.MEDIUM, wolf to Role.WEREWOLF).create().playerManager)
        GameEvent.MediumRevealed.send(wolf, MediumResult.WEREWOLF, self)
        GameEvent.StatementMade.send(1, self.name, Statement.MediumReport(self, wolf, MediumResult.WEREWOLF), allPlayers)

        val result = UnreportedResults(memoriesFor(self), self)

        assertEquals(emptyMap(), result.mediums)
    }
}
