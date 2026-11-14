package werewolf.ai

import werewolf.game.DiscussionContext
import werewolf.game.Eagerness
import werewolf.game.Role
import werewolf.game.UrgeEligibility

data class ParsedUrge(val eagerness: Eagerness, val intent: String, val counterClaimRole: Role?)

class UrgeFormat {
    private enum class Level(val eagerness: Eagerness) {
        COUNTER_CLAIM(Eagerness.COUNTER_CLAIM) {
            override fun isSelectable(eligibility: UrgeEligibility) = eligibility.counterClaimRoles.isNotEmpty()
            override fun description(eligibility: UrgeEligibility): String {
                val roleNames = eligibility.counterClaimRoles.joinToString("、") { it.displayName }
                return "対抗COする（他の人の役職COに続いて、同じ役職をすぐさまCOする。対抗COできる役職：$roleNames）"
            }
        },
        ANNOUNCEMENT(Eagerness.ANNOUNCEMENT) {
            override fun isSelectable(eligibility: UrgeEligibility) = eligibility.canAnnounce
            override fun description(eligibility: UrgeEligibility) = "役職COや、占い・霊媒結果の報告をする"
        },
        URGENT(Eagerness.URGENT) {
            override fun isSelectable(eligibility: UrgeEligibility) = true
            override fun description(eligibility: UrgeEligibility) =
                "今すぐ言うべきことがある（自分への疑いへの反論、名指しで問われたことへの返答など）"
        },
        OPINION(Eagerness.OPINION) {
            override fun isSelectable(eligibility: UrgeEligibility) = true
            override fun description(eligibility: UrgeEligibility) = "言いたい意見・質問がある"
        },
        NOTHING(Eagerness.NOTHING) {
            override fun isSelectable(eligibility: UrgeEligibility) = true
            override fun description(eligibility: UrgeEligibility) = "今は特に言うことがない"
        },
        ;

        abstract fun isSelectable(eligibility: UrgeEligibility): Boolean
        abstract fun description(eligibility: UrgeEligibility): String
    }

    private companion object {
        // 「5」または「5（占い師）」のように、段階の数字に役職名を添えられる
        val LEVEL_PATTERN = Regex("""^(\d+)(?:[（(](.+)[）)])?$""")
    }

    fun buildInstruction(context: DiscussionContext, eligibility: UrgeEligibility): String {
        val levels = selectableLevels(eligibility)
        return listOf(
            "【${context.title} - 発言意欲】次の発言者を決めます。今どれだけ発言したいかを、以下の段階から一つ選んでください。",
            "最も高い段階を選んだ人が発言します（同じ段階の人が複数いればランダム）。",
            levels.joinToString("\n") { "${it.eagerness.value}：${it.description(eligibility)}" },
            "",
        ).plus(answerFormat(levels)).joinToString("\n")
    }

    private fun answerFormat(levels: List<Level>): List<String> {
        val counterClaimFormat = if (Level.COUNTER_CLAIM in levels) {
            "対抗COする場合は「理由：${Level.COUNTER_CLAIM.eagerness.value}（役職名）」の形式で答えてください。"
        } else {
            ""
        }
        return listOf(
            "「理由（50文字以内）：段階の数字」の形式で答えてください。$counterClaimFormat",
            "例：占い師COしたZoeに、狂人として対抗COするため：${Level.COUNTER_CLAIM.eagerness.value}（占い師）",
            "例：Zackからの質問に答えるため：${Level.URGENT.eagerness.value}",
        )
    }

    fun parse(input: String, eligibility: UrgeEligibility): ParsedUrge {
        val (intent, answer) = splitReason(input)
        val (levelNumber, roleName) = splitLevelAndRole(answer)
        val level = selectableLevelOf(levelNumber, eligibility)
        return ParsedUrge(level.eagerness, intent, roleOf(level, roleName, eligibility))
    }

    private fun splitLevelAndRole(answer: String): Pair<String, String> {
        val (levelNumber, roleName) = LEVEL_PATTERN.matchEntire(answer)?.destructured
            ?: throw InvalidAiInputException("段階の数字ではありません: $answer")
        return levelNumber to roleName
    }

    private fun selectableLevelOf(levelNumber: String, eligibility: UrgeEligibility): Level =
        selectableLevels(eligibility).singleOrNull { it.eagerness.value.toString() == levelNumber }
            ?: throw InvalidAiInputException("選択できない段階です: $levelNumber")

    private fun roleOf(level: Level, roleName: String, eligibility: UrgeEligibility): Role? =
        if (level == Level.COUNTER_CLAIM) counterClaimRoleOf(roleName, eligibility) else rejectRole(roleName)

    private fun splitReason(input: String): Pair<String, String> {
        val separatorIdx = maxOf(input.lastIndexOf("："), input.lastIndexOf(":"))
            .takeIf { it >= 0 }
            ?: throw InvalidAiInputException("「理由：段階の数字」の形式ではありません: $input")
        return input.substring(0, separatorIdx).trim() to input.substring(separatorIdx + 1).trim()
    }

    private fun counterClaimRoleOf(roleName: String, eligibility: UrgeEligibility): Role {
        if (roleName.isEmpty()) throw InvalidAiInputException("対抗COする役職が添えられていません")
        return eligibility.counterClaimRoles.singleOrNull { it.displayName == roleName }
            ?: throw InvalidAiInputException("対抗COできない役職です: $roleName")
    }

    private fun rejectRole(roleName: String): Role? {
        if (roleName.isNotEmpty()) throw InvalidAiInputException("対抗CO以外の段階に役職が添えられています: $roleName")
        return null
    }

    private fun selectableLevels(eligibility: UrgeEligibility): List<Level> =
        Level.entries.filter { it.isSelectable(eligibility) }
}
