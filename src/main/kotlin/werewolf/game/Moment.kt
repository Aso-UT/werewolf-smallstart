package werewolf.game

// 出来事が起きた時点。今この瞬間からしか作れないので、任意の時刻をでっち上げることはできない
class Moment private constructor(private val nanos: Long) : Comparable<Moment> {
    override fun compareTo(other: Moment) = nanos.compareTo(other.nanos)

    companion object {
        fun now() = Moment(System.nanoTime())
    }
}
