# テスト戦略

## 何をテストするか

**ロジックの複雑さ・境界値・ミスが起きうる箇所**を優先してテストする。

## テストしないもの

以下はテストを書かない。

- **`ConsoleHumanIO`**: stdin/stdout を直接扱うため自動テストが難しい。
  設計上ここにロジックを置かないことでテストの必要性を最小化している。
- **`werewolf.human.web` / `werewolf.lodge.WebHumanConnection`**:
  Ktor サーバーや WebSocket チャネルなどインフラに依存しており、
  ユニットテストの対象としてなじまない。
  設計上ここにロジックを置かないことでテストの必要性を最小化している。

それ以外のコードは、テスト実装（後述）を使えば基本的にテスト可能。

## テストのレイヤー

### サブユニットテスト

複雑なロジックや境界値を持つ単一クラスを直接テストする。

| クラス / オブジェクト | テストの観点の例 |
|----------------------|----------------|
| `WinConditionChecker` | 人狼0→市民勝利、人狼≧市民→人狼勝利、ゲーム継続中はnull |
| `Side.hasWon()` | 各陣営の勝利条件の境界値 |
| `SelectionContext.*` | `candidates()` が自分自身・除外対象を正しく除くか |
| `AliveCounts` | 各陣営のカウントが正しく算出されるか |
| `MajorityVoteResolver` | 最多票・同数・空リストの各ケース |

### フェーズテスト

`RecordingPlayer` などのテスト実装を使い、フェーズ単体を `proceed()` して
「何を誰に送り、どう状態を変えるか」を検証する。

フェーズをまたいだゲーム全体のテストは、セットアップコストが高い割に
各フェーズテストで十分カバーできるため書かない。

```kotlin
// テスト実装の例
private class RecordingPlayer(role: Role, override val name: String) : Player(role) {
    val received = mutableListOf<GameEvent>()
    override fun selectTarget(context: SelectionContext) = this
    override fun onReceive(event: GameEvent) { received.add(event) }
    override fun discuss(players: List<Player>) = ""
}
```

## いつ書くか

機能追加・バグ修正のタイミングで、**変更対象のコードにテストを合わせて追加する**。

- 新しいロジックを追加したら、同じPRにテストを含める
- 既存ロジックを変更したら、対応するテストも更新する
- テストが書きにくいと感じたら、それは設計を見直すサインとして受け取る

## テストの置き場所

```
src/test/kotlin/
```

パッケージは `org.example`（mainと同じ）を使用する。

## テストプレイヤーの命名

- name・変数名ともに英語表記とする
- role名（enum名と一致しない略称も可: Wolf など）をデフォルトとする
- 同じroleが複数いる場合はrole名（または頭文字）＋連番: V1, V2, Wolf1, Wolf2
- テスト固有の事情でroleより適切な名前がある場合はこの限りでない（例: 処刑対象を Victim と名付ける、議論ログを読みやすくするために人名を使うなど）

## テストフレームワーク

`kotlin("test")`（JUnit Platform経由）を使用する。`build.gradle.kts` に設定済み。
