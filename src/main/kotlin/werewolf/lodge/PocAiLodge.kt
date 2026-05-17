package werewolf.lodge

import werewolf.game.Player
import werewolf.game.Role
import werewolf.ai.AiPlayer
import werewolf.ai.Instruction
import werewolf.ai.PocConsoleLanguageModel

object PocAiLodge : Lodge() {
    private val names = listOf("Alice", "Bob", "Charlie", "Dave", "Eve", "Frank", "Grace", "Heidi", "Ivan")

    private val personalities = listOf(
        "あなたは注意深いプレイヤーです。議論の流れとは逆の可能性を常に検討し、発言に活かしてください。",
        "あなたは計算高いプレイヤーです。生存者数と推定人狼数から残り何回処刑できるかを考え、議論に活かしてください。",
        "あなたは積極的なプレイヤーです。自分の意見をはっきり主張し、議論をリードしてください。",
        "あなたは観察眼の鋭いプレイヤーです。他プレイヤーの発言の矛盾や不自然さに注目して議論してください。",
        "あなたは慎重なプレイヤーです。確証のないことは断言せず、可能性として提示することを心がけてください。",
        "あなたは感情的なプレイヤーです。直感を大切にし、勢いよく発言してください。",
        "あなたは論理的なプレイヤーです。情報を整理して筋道立てた推理を展開してください。",
        "あなたは協調的なプレイヤーです。他プレイヤーの意見を尊重しつつ、村全体の合意形成を意識してください。",
        "あなたは疑い深いプレイヤーです。他プレイヤーの発言を鵜呑みにせず、動機や裏の意図を考えてください。",
    )

    override fun assignments(): List<Pair<Player, Role>> {
        val roles = listOf(
            Role.HUNTER, Role.VILLAGER, Role.VILLAGER, Role.VILLAGER, Role.MADMAN,
            Role.WEREWOLF, Role.SEER, Role.MEDIUM, Role.WEREWOLF,
        ).shuffled()
        return roles.mapIndexed { i, role ->
            AiPlayer(role, names[i], PocConsoleLanguageModel(), Instruction(names[i], personalities[i])) to role
        }
    }
}
