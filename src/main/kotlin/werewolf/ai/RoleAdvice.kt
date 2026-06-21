package werewolf.ai

import werewolf.game.ChronicleView
import werewolf.game.Recallable
import werewolf.game.RecallView
import werewolf.game.Role

class RoleAdvice(private val recipientName: String, private val role: Role, private val text: String) : Recallable() {
    override fun toRecallView() = RecallView.Observation("戦略アドバイス", text)
    override fun toChronicleView() = ChronicleView.Observation(recipientName, "${role.displayName}の戦略アドバイス", text)

    companion object {
        fun random(role: Role, name: String) = RoleAdvice(name, role, pool.getValue(role).random())

        private val pool: Map<Role, List<String>> = mapOf(
            Role.VILLAGER to listOf(
                "積極的に発言して情報を集め、他プレイヤーの発言の矛盾や不自然さを観察してください。占い師・霊能者・狩人を名乗るプレイヤーの主張が一致しているか確認し、怪しいプレイヤーを絞り込んでください。",
                "序盤は他プレイヤーの発言をよく聞き、不自然な主張がないか観察してください。発言の流れを分析し、人狼が誰かを議論でリードしてください。",
                "確定情報（占い・霊能結果）を最大限活用して議論してください。感情論ではなく論理的な根拠で処刑候補を絞り込んでください。",
            ),
            Role.WEREWOLF to listOf(
                "序盤は村人として自然に振る舞い、潜伏を基本戦略としてください。狂人が存在するため、占い師や霊能者を騙るのは狂人に任せ、人狼は村人を装うことを優先してください。",
                "序盤は目立たず、他プレイヤーの疑いを別の誰かに向けさせてください。終盤は密談で連携し、村人側の票を操作してください。",
                "村人側の議論に自然に乗りつつ、疑いを無実の村人に誘導してください。占い師を早期に処刑させることが最重要目標です。",
            ),
            Role.SEER to listOf(
                "占い結果を使って村をリードしてください。早期に名乗り出ると人狼に狙われやすいため、タイミングを慎重に判断してください。黒判定が出た場合は積極的に開示を検討してください。",
                "黒判定が出たら積極的に開示し、村の処刑先を誘導してください。名乗り出る際は狩人に護衛を依頼してください。",
                "序盤は潜伏しつつ占い情報を蓄積し、決定的な証拠が出たタイミングで主張してください。",
            ),
            Role.MEDIUM to listOf(
                "処刑者の霊視結果を議論に活かしてください。占い師の主張と霊能結果が矛盾していれば、偽占い師の可能性を指摘してください。",
                "比較的早めに名乗り出て占い師との整合性を示すことで、村の信頼を得やすくなります。霊能結果を積極的に共有してください。",
                "霊能結果を使って偽占い師（狂人）を炙り出してください。占い師が複数名乗り出た場合、霊能結果との整合性で本物を見極めてください。",
            ),
            Role.HUNTER to listOf(
                "序盤は役職を明かさず潜伏してください。護衛先は占い師や霊能者を優先し、人狼の襲撃から守ってください。",
                "占い師が名乗り出たら積極的に護衛してください。護衛先を変えながら人狼の動向を観察してください。",
                "潜伏を維持しつつ護衛を続けてください。自分が狙われにくいよう、発言は控えめにしてください。",
            ),
            Role.MADMAN to listOf(
                "占い師を騙って本物の占い師への不信を植え付け、村の議論を混乱させてください。人狼の正体は知りませんが、人狼陣営の勝利を目指してください。",
                "村人として振る舞いながら、疑いを無実の村人に向けさせてください。あからさまに怪しい行動は避け、自然に村を混乱させてください。",
                "占い師や霊能者を騙ることで、村人側の確定情報を崩してください。人狼が処刑されないよう、議論の流れを操作してください。",
            ),
        )
    }
}
