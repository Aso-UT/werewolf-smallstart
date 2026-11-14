package werewolf.ai

import werewolf.game.ActionMetadata
import werewolf.game.ChronicleView
import werewolf.game.Moment
import werewolf.game.Player
import werewolf.game.PlayerAction
import werewolf.game.Recallable

// 反芻：本番の呼び出しの合間に、ゲームの流れを読み返してプロンプトキャッシュを保つこと。
// モデルはプレイヤーを知らないので、送った時点と付随情報だけを残し、プレイヤーが Rumination として記録する
class RuminationRecord(val occurredAt: Moment, val metadata: ActionMetadata)

class Rumination(private val actor: Player, record: RuminationRecord) : Recallable(record.occurredAt), PlayerAction {
    override val metadata = record.metadata
    override fun toRecallView() = error("Rumination is not stored in AI memory and should not appear in prompts")
    override fun toChronicleView() = ChronicleView.Action(actor.name, "反芻", "", metadata.toDisplayString())
}
