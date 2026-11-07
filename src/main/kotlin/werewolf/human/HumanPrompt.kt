package werewolf.human

import werewolf.view.ChoiceView

interface HumanPrompt {
    fun promptChoice(view: ChoiceView): String
    fun promptFreeText(title: String, description: String): String
}
