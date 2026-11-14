package werewolf.ai

interface LanguageModel {
    fun ask(system: String, history: List<String>, instruction: String): Completion

    // 本番の呼び出しの合間に送った反芻を受け取る。受け取った分はモデルから取り除かれる
    fun drainRuminations(): List<RuminationRecord>

    // 反芻をやめる。送っている途中の反芻があれば、終わるまで待ってから戻る。2回目以降は何もしない
    fun stop()
}
