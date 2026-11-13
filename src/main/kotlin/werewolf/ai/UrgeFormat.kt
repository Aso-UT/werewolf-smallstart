package werewolf.ai

import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.UrgeEligibility

data class ParsedUrge(val eagerness: Eagerness, val intent: String)

class UrgeFormat {
    private enum class Level(val eagerness: Eagerness, val description: String) {
        COUNTER_CLAIM(Eagerness.COUNTER_CLAIM, "対抗COする（他の人の役職COに続いて、同じ役職をすぐさまCOする）") {
            override fun isSelectable(eligibility: UrgeEligibility) = eligibility.counterClaimRole != null
        },
        ANNOUNCEMENT(Eagerness.ANNOUNCEMENT, "役職COや、占い・霊媒結果の報告をする") {
            override fun isSelectable(eligibility: UrgeEligibility) = eligibility.canAnnounce
        },
        URGENT(Eagerness.URGENT, "今すぐ言うべきことがある（自分への疑いへの反論、名指しで問われたことへの返答など）") {
            override fun isSelectable(eligibility: UrgeEligibility) = true
        },
        OPINION(Eagerness.OPINION, "言いたい意見・質問がある") {
            override fun isSelectable(eligibility: UrgeEligibility) = true
        },
        NOTHING(Eagerness.NOTHING, "今は特に言うことがない") {
            override fun isSelectable(eligibility: UrgeEligibility) = true
        },
        ;

        abstract fun isSelectable(eligibility: UrgeEligibility): Boolean
    }

    fun buildInstruction(context: DiscussionContext, eligibility: UrgeEligibility): String {
        val levels = selectableLevels(eligibility)
        return listOf(
            "【${context.title} - 発言意欲】次の発言者を決めます。今どれだけ発言したいかを、以下の段階から一つ選んでください。",
            "最も高い段階を選んだ人が発言します（同じ段階の人が複数いればランダム）。",
            levels.joinToString("\n") { "${it.eagerness.value}：${it.description}" },
            "",
            "「理由（50文字以内）：段階の数字」の形式で答えてください。",
            "例：占い師COしたZoeに、狂人として対抗COするため：${Level.COUNTER_CLAIM.eagerness.value}",
            "例：Zackからの質問に答えるため：${Level.URGENT.eagerness.value}",
        ).joinToString("\n")
    }

    fun parse(input: String, eligibility: UrgeEligibility): ParsedUrge {
        val separatorIdx = maxOf(input.lastIndexOf("："), input.lastIndexOf(":"))
            .takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「理由：段階の数字」の形式ではありません: $input")
        val intent = input.substring(0, separatorIdx).trim()
        val levelString = input.substring(separatorIdx + 1).trim()
        val level = selectableLevels(eligibility).singleOrNull { it.eagerness.value.toString() == levelString }
            ?: throw InvalidAiInputException("選択できない段階です: $levelString")
        return ParsedUrge(level.eagerness, intent)
    }

    private fun selectableLevels(eligibility: UrgeEligibility): List<Level> =
        Level.entries.filter { it.isSelectable(eligibility) }
}
