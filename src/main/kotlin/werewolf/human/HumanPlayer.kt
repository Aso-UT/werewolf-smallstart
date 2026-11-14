package werewolf.human

import werewolf.game.Choice
import werewolf.game.Claim
import werewolf.game.ChronicleView
import werewolf.game.DiscussionContext
import werewolf.game.GameEvent
import werewolf.game.GameOverSignal
import werewolf.game.Player
import werewolf.game.ReportEligibility
import werewolf.game.Role
import werewolf.game.SelectionContext
import werewolf.game.SpeakingUrge
import werewolf.game.UrgeEligibility
import kotlin.time.measureTimedValue

class HumanPlayer(role: Role, override val name: String, private val io: HumanIO) : Player(role) {
    companion object {
        private const val URGE_SECONDS = 5
    }

    private val screen = HumanScreen(io)
    private val selection = HumanSelection(io)
    private val composer = StatementComposer(io, selection)

    override fun choose(context: SelectionContext): Choice {
        screen.showSelectionMood(context)
        val (selected, metadata) = measured(HumanActionKind.CHOICE) {
            selection.select(context.title, context.description, context.candidates()) { it.name }
        }
        val choice = Choice(this, context, selected, "プレイヤーが選択", metadata)
        io.display(choice.toRecallView())
        return choice
    }

    override fun onReceive(event: GameEvent) {
        screen.show(event)
    }

    override fun feelUrge(context: DiscussionContext, eligibility: UrgeEligibility): SpeakingUrge {
        val buttons = UrgeButton.available(eligibility)
        val (pressed, metadata) = measured(HumanActionKind.SPEAKING_URGE) { pressedButton(context, buttons) }
        return pressed.press(this, context, metadata)
    }

    private fun pressedButton(context: DiscussionContext, buttons: List<UrgeButton>): UrgeButton {
        val (hidden, shown) = buttons.partition { it.isHidden }
        val label = io.promptSpeakingUrge(context.title, URGE_SECONDS, shown.map { it.label }, hidden.single().label)
        return buttons.single { it.label == label }
    }

    override fun speak(context: DiscussionContext, claimableRoles: Set<Role>, reportEligibility: ReportEligibility): Claim {
        val (statement, metadata) = measured(HumanActionKind.STATEMENT) {
            composer.compose(this, context, claimableRoles, reportEligibility)
        }
        return Claim(this, context, statement, claimableRoles, reportEligibility, "プレイヤーが発言", metadata)
    }

    // 問い合わせに答えられるようになってから、行動が決まるまでの時間を測る
    private fun <T> measured(kind: HumanActionKind, decide: () -> T): Pair<T, HumanActionMetadata> {
        io.waitUntilReady()
        val (decided, responseTime) = measureTimedValue(decide)
        return decided to HumanActionMetadata(kind, responseTime)
    }

    override fun watchEpilogue(chronicles: List<ChronicleView>, metadataTotals: List<String>) {
        io.watchEpilogue(chronicles, metadataTotals)
    }

    override fun onRevealing(signal: GameOverSignal) { /* human players record everything as they act */ }
}
