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
フロントエンドファイルを変更した場合は `./gradlew npmBuild`（脆弱性チェック＋型チェック＋ビルド）も実行されます。

## 実行

```bash
./gradlew npmBuild   # フロントエンドをビルド（初回および変更時）
./gradlew run        # サーバー起動 → http://localhost:8080
```

> **Note:** `npmBuild` は内部で `npm ci`（lockfile 通りの厳密インストール）・脆弱性チェック・型チェックを自動実行します。Node.js が必要です。

### 依存パッケージの更新（メンテナンス時のみ）

```bash
./gradlew npmUpdateDeps   # package-lock.json を更新し脆弱性チェックを実行
git diff frontend/svelte/package-lock.json   # 変更内容を確認してからコミット
```

### Web UI 開発時（Svelte ファイルを編集しながら確認する場合）

```bash
./gradlew run                              # ターミナル1：Kotlin サーバー起動
cd frontend/svelte && npm run dev          # ターミナル2：Vite dev server 起動
# http://localhost:5173 をブラウザで開く
# .svelte を編集・保存するたびブラウザが自動更新される
```

## テスト・静的解析

```bash
./gradlew check
```

## 開発フロー

ブランチ戦略・PR・issue のルールは [`.github/WORKFLOW.md`](.github/WORKFLOW.md) を参照してください。

テスト方針は [`docs/TESTING.md`](docs/TESTING.md) を参照してください。