# werewolf-smallstart

Kotlin製の人狼ゲームエンジンです。

## 目標

- **環境非依存**: プレイヤーとのやり取りを `PlayerIO` 経由に統一することで、コンソール・Web・その他のフロントエンドへの差し替えを可能にする
- **多様なプレイヤー**: 人間プレイヤーだけでなく、ルールベースのCPUや AIをプレイヤーとして参加させられる設計を目指す

現時点ではコンソール上で動作する実装が含まれています。

## セットアップ

```bash
git config core.hooksPath .githooks
```

pre-commit フックが有効になり、コミット前に `./gradlew check`（静的解析＋テスト）が自動実行されます。

## 実行

```bash
./gradlew run
```

## テスト・静的解析

```bash
./gradlew check
```

## 開発フロー

ブランチ戦略・PR・issue のルールは [`.github/WORKFLOW.md`](.github/WORKFLOW.md) を参照してください。

テスト方針は [`docs/TESTING.md`](docs/TESTING.md) を参照してください。