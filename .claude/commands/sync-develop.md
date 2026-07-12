---
description: マージ済みのPRをローカルのdevelopに反映する（git checkout develop && git pull origin develop）
---

ローカルの `develop` ブランチをリモートの最新状態に同期する。以下の手順を必ずこの順番で実行すること。

1. `git status` で作業ツリーに未コミット・未追跡の変更がないか確認する。
   - コミットされていない変更がある場合は、勝手にstash/破棄せずユーザーに報告して指示を仰ぐ。
2. 現在のブランチが `develop` でなければ `git checkout develop` する。
3. `git pull origin develop` を実行する。
4. `git log --oneline -5` で取得後の履歴を確認し、直近でマージされたはずのPRの内容が反映されているかを一言で報告する。
   - 反映されていない場合（"Already up to date" なのに期待した変更が見当たらない等）は、その旨とありうる原因（PRがまだマージされていない、ブランチ名の勘違い等）を伝える。
