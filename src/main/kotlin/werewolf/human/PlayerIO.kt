package werewolf.human

import werewolf.game.ChronicleView
import werewolf.game.RecallView
import werewolf.view.ChoiceView

abstract class PlayerIO {
    abstract fun display(view: RecallView)
    abstract fun sendMessage(title: String, content: String)
    protected abstract fun readInput(): String
    abstract fun promptChoice(view: ChoiceView): String
    abstract fun watchEpilogue(chronicles: List<ChronicleView>)

    fun promptFreeText(title: String, content: String): String {
        sendMessage(title, content)
        return readInput()
    }
}
