---
description: openなPRについた人とSonarCloudのレビューコメントを確認し、対応方針を整理して提示する
---

openなPRについたレビューコメントを確認し、対応方針を提示する。コード編集はユーザーが方針に回答してから行う。以下の手順を順番に実行すること。

## 1. 対象PRを特定する

- `gh pr list --state open` でopenなPRを探す。
  - 1件ならそのPRを対象にする。
  - 複数ある場合は、現在のブランチ（`git branch --show-current`）に対応するPRを優先する。判断できなければユーザーにどのPRか確認する。
  - 0件ならその旨を報告して終了する。
- 対象PRのブランチが現在のブランチと異なる場合は、勝手にcheckoutせずユーザーに報告する。

## 2. コメントを集める

コメントは「人のコメント」と「SonarCloudのコメント」の2種類があり、両方を確認する。

### 人のコメント

人のコメントはファイル内コメント（インラインのレビューコメント）であることが多いので、会話タブのコメントだけで済ませず、必ず両方を見る。

- 会話タブのコメントとレビュー本文: `gh pr view <番号> --json comments,reviews`
- ファイル内コメント: `gh api repos/Aso-UT/werewolf-smallstart/pulls/<番号>/comments`
  - `path`・`line`（`line` がnullなら `original_line`）・`diff_hunk` で対象箇所を把握する。
  - `in_reply_to_id` があるものは返信なので、元のコメントとスレッドとしてまとめて読む。
- 自分（Claude）が過去に投稿した「🤖 Claude:」で始まるコメントや、既に対応済みと読み取れるスレッドは区別して扱う。

### SonarCloudのコメント

- PRには `sonarqubecloud` ユーザーがQuality Gateのサマリーコメントを付ける。サマリーには件数しか出ないので、詳細はSonarCloudのAPIで取得する。
  - Issues: `curl -s "https://sonarcloud.io/api/issues/search?componentKeys=Aso-UT_werewolf-smallstart&pullRequest=<番号>&resolved=false"`
  - Security Hotspots: `curl -s "https://sonarcloud.io/api/hotspots/search?projectKey=Aso-UT_werewolf-smallstart&pullRequest=<番号>"`
  - `component`（ファイル）・`line`・`rule`・`message`・`severity` を把握する。
- サマリーにカバレッジや重複率でQuality Gateが落ちている旨があれば、それも指摘として扱う。カバレッジは100%を目指さない方針（数値埋めのテストは書かない）を踏まえて判断する。

## 3. 対象コード・関連コードを確認する

- 各コメントについて、指摘箇所のファイルを読み、周辺のコードと文脈を確認する。
- 同じパターンが他の箇所にもないか、指摘を反映すると影響が及ぶ呼び出し元・テストがないかも確認する。
- 必要なら `git diff develop...HEAD` でPR全体の変更内容を確認する。

## 4. 対応方針を整理して提示する

コメントごとに以下を整理してユーザーに提示する。

- 出どころ（人 / SonarCloud）と対象箇所（`ファイルパス:行番号`）
- 指摘内容の要約
- 対応方針（具体的にどう直すか。複数案があれば推奨案を示す）
- 対応しない方がよいと考える指摘や、意図が読み取りにくい指摘は、その理由や確認したい点を明記する

コメントが1件もなければ、その旨を報告して終了する。

**この時点ではコードを編集しない。** ユーザーの回答を待ち、合意した方針に沿ってコード編集を進める。
GitHubへの返信コメント投稿やスレッドのresolveも、ユーザーの指示がない限り行わない。
