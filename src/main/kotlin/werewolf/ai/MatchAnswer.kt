package werewolf.ai

import werewolf.util.Cardinality
import werewolf.util.cardinality

// AIの答えに当てはまる選択肢を一つに決める。
// 当てはまるものがなければAIの答えの誤りなので聞き直す。複数あれば選択肢の作り方の誤り
fun <T> Iterable<T>.matchAnswer(invalidMessage: String, predicate: (T) -> Boolean): T =
    when (val matched = filter(predicate).cardinality()) {
        Cardinality.None -> throw InvalidAiInputException(invalidMessage)
        is Cardinality.One -> matched.value
        is Cardinality.Many -> error("Choices must be distinguishable, but the answer matched ${matched.values}")
    }
