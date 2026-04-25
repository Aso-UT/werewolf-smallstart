package org.example

//TIP コードを<b>実行</b>するには、<shortcut actionId="Run"/> を押すか
// ガターの <icon src="AllIcons.Actions.Execute"/> アイコンをクリックします。
fun main() {
    val setup = SmallLodge.create()
    val manager = PlayerManager(setup)
    InitialPhase(setup.oracle).proceed()
    var nightNumber = 1
    try {
        while (true) {
            manager.runTurn(nightNumber)
            nightNumber++
        }
    } catch (signal: GameOverSignal) {
        manager.endGame(signal)
    }
}