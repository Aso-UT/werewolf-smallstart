package werewolf.ai

import werewolf.game.ChronicleView
import werewolf.game.Recallable
import werewolf.game.RecallView

class Instruction(private val recipientName: String, private val text: String) : Recallable() {
    override fun toRecallView() = RecallView.Observation("指示", text)
    override fun toChronicleView() = ChronicleView.Observation(recipientName, "指示", text)
}
