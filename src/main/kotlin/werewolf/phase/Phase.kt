package werewolf.phase


fun interface Phase {
    fun proceed(): Phase
}
