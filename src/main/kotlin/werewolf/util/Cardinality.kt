package werewolf.util

// コレクションの要素が、なし・1つ・複数のどれかを表す。when で3通りを並べて書くために使う
sealed interface Cardinality<out T> {
    data object None : Cardinality<Nothing>
    data class One<T>(val value: T) : Cardinality<T>
    data class Many<T>(val values: List<T>) : Cardinality<T>
}

fun <T> Iterable<T>.cardinality(): Cardinality<T> {
    val values = toList()
    return when (values.size) {
        0 -> Cardinality.None
        1 -> Cardinality.One(values.first())
        else -> Cardinality.Many(values)
    }
}
