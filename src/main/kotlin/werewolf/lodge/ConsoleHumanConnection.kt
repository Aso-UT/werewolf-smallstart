package werewolf.lodge

import werewolf.human.HumanIO
import werewolf.human.console.ConsoleHumanIO

class ConsoleHumanConnection : HumanConnection {
    override fun createIO(): HumanIO = ConsoleHumanIO()
    override fun setup() { /* Console I/O is managed by the JVM and requires no setup */ }
    override fun teardown() { /* Console I/O is managed by the JVM and requires no teardown */ }
}
