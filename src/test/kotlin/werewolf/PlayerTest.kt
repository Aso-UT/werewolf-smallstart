package werewolf

import werewolf.game.*

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PlayerTest {

    private class ArbitraryChooserSetter(
        role: Role,
        name: String,
        private val chooserToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(chooserToSet, context)
    }

    private class ChoosingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun choose(context: SelectionContext): Choice = FallbackChoice(this, context)
    }

    private class ArbitrarySpeakerSetter(
        role: Role,
        name: String,
        private val speakerToSet: Player,
    ) : NothingPlayer(role, name) {
        override fun speak(context: DiscussionContext): Claim =
            Claim(speakerToSet, context, Statement.Plain(""), "")
    }

    private class SpeakingPlayer(role: Role, name: String) : NothingPlayer(role, name) {
        override fun speak(context: DiscussionContext): Claim =
            Claim(this, context, Statement.Plain(""), "")
    }

    @Test
    fun `selectTarget throws when choose returns a choice with wrong chooser`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitraryChooserSetter(Role.VILLAGER, "Player", chooserToSet = otherPlayer)
        val context = SelectionContext.Vote(player, listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.selectTarget(context) }
    }

    @Test
    fun `selectTarget records choice in player memories`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ChoosingPlayer(Role.VILLAGER, "Player")
        val context = SelectionContext.Vote(player, listOf(player, otherPlayer))
        player.selectTarget(context)
        assertTrue(player.reveal(fakeCitizenWinSignal()).any { it is Choice })
    }

    @Test
    fun `discuss throws when speak returns a claim with wrong speaker`() {
        val otherPlayer = NothingPlayer(Role.VILLAGER, "OtherPlayer")
        val player = ArbitrarySpeakerSetter(Role.VILLAGER, "Player", speakerToSet = otherPlayer)
        val context = openContext(listOf(player, otherPlayer))
        assertFailsWith<IllegalArgumentException> { player.discuss(context) }
    }

    @Test
    fun `discuss records claim in player memories`() {
        val player = SpeakingPlayer(Role.VILLAGER, "Player")
        player.discuss(openContext(listOf(player)))
        assertTrue(player.reveal(fakeCitizenWinSignal()).any { it is Claim })
    }
}
