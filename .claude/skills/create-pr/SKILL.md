---
name: create-pr
description: 作業ブランチの変更をコミット・プッシュし、.github/pull_request_template.mdのテンプレートに準拠したプルリクエストをgh CLIで作成する。「プルリクを作成して」「PRを作って」「プルリクエストを出して」などのキーワードで起動する。
model: sonnet
---

# プルリクエスト作成

作業ブランチの変更を`.github/pull_request_template.md`のフォーマットに沿ってmainブランチ向けのプルリクエストにするスキル。

## 事前条件の確認

以下をBashツールで確認し、満たさない場合はプルリクエストを作成せずユーザーに報告する。

1. **作業ブランチにいること**: `git branch --show-current`がmain以外であること。mainの場合は中断する。
2. **テストがパスしていること**: 直前の`test-runner`の結果でJUnitのNG件数とCheckstyle違反件数がいずれも0件であること。結果が未取得の場合は、Agentツールで`test-runner`サブエージェント（`subagent_type: "test-runner"`）を呼び出して確認する。NGが1件以上ある場合は中断する。

## 実行手順

1. Readツールで`.github/pull_request_template.md`を読み込み、セクション構成とHTMLコメントの指示（記載量の上限）を確認する。
2. 未コミットの変更がある場合はコミットする。
   - `git status`と`git diff HEAD`で変更内容を確認する。
   - コミットメッセージは`commit-message`スキルに従い、Conventional Commits形式の日本語で作成する。
   - 変更ファイルは個別に指定してステージングする（`git add -A`は使用しない）。
3. `git push -u origin {ブランチ名}`で作業ブランチをリモートへプッシュする。
4. `git diff main...HEAD --stat`および`git log main..HEAD --oneline`で、mainとの差分全体を把握する。直近のコミットだけでなく、ブランチ上の全コミットを対象とする。
5. タイトルとIssue番号を決定する。
   - **タイトル**: `commit-message`スキルに従い、Conventional Commits形式の日本語で記載する（例: `feat: TCPエコーサーバーを追加`）。70文字以内とする。
   - **Issue番号**: ブランチ名（`<type>/#<Issue番号>-<概要>`形式）から抽出する。抽出できない場合は`gh issue list`で該当Issueを確認する。
6. Bashツールで`gh pr create`を実行する。本文はHEREDOCで渡す。

   ```bash
   gh pr create --base main --title "{タイトル}" --body "$(cat <<'EOF'
   ## 概要

   {1〜2文}

   ## 変更内容

   - {1行}

   ## テスト結果

   | 項目 | 結果 |
   | --- | --- |
   | JUnit | 全体 {n}件 / OK {n}件 / NG 0件 |
   | Checkstyle | 違反 0件 |

   Closes #{Issue番号}

   🤖 Generated with [Claude Code](https://claude.com/claude-code)
   EOF
   )"
   ```

7. 作成されたプルリクエストのURLをユーザーに報告する。

## 記載ルール

- テンプレートのセクション構成（`## 概要` / `## 変更内容` / `## テスト結果`）と順序を変更しない。セクションの追加・削除もしない。
- テンプレート内のHTMLコメント（`<!-- -->`）は、指示を読み取った上で**本文には含めない**。
- テスト結果の表には、`test-runner`が返した実際の件数を記載する。値を推測で書かない。
- 文章量は最低限に留める。
  - 概要: 1〜2文
  - 変更内容: 箇条書き5項目以内、各項目1行
- 冗長な前置き・補足説明・所感は書かない。事実と決定事項のみを記載する。

## 注意事項

- `Closes #{Issue番号}`を必ず記載し、マージ時にIssueが自動クローズされるようにする。
- 本文の末尾には`🤖 Generated with [Claude Code](https://claude.com/claude-code)`を付与する。
- mainブランチへの直接プッシュ、および強制プッシュ（`--force`）は行わない。
- プルリクエストのマージは行わない。マージはユーザーが判断する。
