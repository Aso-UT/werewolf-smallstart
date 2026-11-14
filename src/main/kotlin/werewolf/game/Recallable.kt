package werewolf.game

// occurredAt は既定では生成した時点。起きた時点と記録する時点がずれるものは、起きた時点で取っておいた Moment を渡す
abstract class Recallable(val occurredAt: Moment = Moment.now()) {
    abstract fun toRecallView(): RecallView
    abstract fun toChronicleView(): ChronicleView
}
