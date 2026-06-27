package werewolf.lodge

import werewolf.human.HumanIO

interface HumanConnection {
    fun createIO(): HumanIO
    fun setup()
    fun teardown()
}
