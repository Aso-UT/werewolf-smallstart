package werewolf.ai

import werewolf.game.ActionMetadata

sealed class Completion {
    abstract val text: String
    abstract val metadata: ActionMetadata

    // モデルが答え終えた応答。形式が正しいかどうかは、受け取った側で確かめる
    data class Answer(override val text: String, override val metadata: ActionMetadata) : Completion()

    // モデルが答えるのを拒否した応答。text は拒否されるまでに出力された途中までのもので、答えとしては扱わない
    data class Refusal(override val text: String, override val metadata: ActionMetadata) : Completion()
}
