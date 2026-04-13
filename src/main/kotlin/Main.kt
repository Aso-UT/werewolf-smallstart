package org.example

//TIP コードを<b>実行</b>するには、<shortcut actionId="Run"/> を押すか
// ガターの <icon src="AllIcons.Actions.Execute"/> アイコンをクリックします。
fun main() {
    val manager = PlayerManager(SmallLodge.create())
    manager.startGame()
    var nightNumber = 1
    try {
        while (true) {
            manager.runNightActions(nightNumber)
            manager.runVoting()
            nightNumber++
        }
    } catch (signal: GameOverSignal) {
        manager.endGame(signal)
    }
}