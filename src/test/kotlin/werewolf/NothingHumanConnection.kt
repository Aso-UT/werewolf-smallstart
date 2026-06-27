package werewolf

import werewolf.human.HumanIO
import werewolf.lodge.HumanConnection

object NothingHumanConnection : HumanConnection {
    override fun createIO(): HumanIO = error("createIO must not be called")
    override fun setup(): Nothing = error("setup must not be called")
    override fun teardown(): Nothing = error("teardown must not be called")
}
