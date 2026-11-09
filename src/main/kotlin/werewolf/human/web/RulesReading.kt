package werewolf.human.web

import java.util.concurrent.CountDownLatch

// ルールを読んでいる間に問い合わせ（発言意欲のカウントを含む）が始まらないよう、読み終えるまで問い合わせを待たせる。
// 問い合わせのないAI同士の進行（初日の密談・夜の行動）は止めない
class RulesReading {
    private val finished = CountDownLatch(1)

    fun finish() = finished.countDown()

    fun waitUntilFinished() = finished.await()
}
