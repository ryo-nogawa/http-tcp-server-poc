---
name: code-review
description: 未コミットのJava変更のレビューをcodex CLIのヘッドレスモードに委託し、MUST・SHOULD・WANTに分類した結果をtarget/code-review-result.mdへ出力する。コードレビュー、実装後のセルフチェック、未コミット変更の規約適合性確認を依頼されたときに使用する。
---

# コードレビュー

レビューを自分で実施せず、**codex CLIのヘッドレスモード（`codex exec`）に委託する**スキル。

レビュー観点・指摘レベル・出力形式の実体は`.agents/skills/code-review/SKILL.md`にあり、codexは`$code-review`記法でそれを直接読み込む。本スキルにレビュー観点を重複して定義しない。

## 事前条件の確認

以下をBashツールで確認し、満たさない場合はレビューを実行せずユーザーに報告する。

1. **codexが使用できること**: `command -v codex`でコマンドの存在を確認する。
2. **レビュー対象があること**: `git status --short`で未コミットのJava変更を確認する。対象が1件もない場合は中断する。

## 実行手順

1. Bashツールで次のコマンドを、**`run_in_background: true`で実行する**（レビューは10分以上かかることがあり、前面実行ではタイムアウトするため）。

   ```bash
   codex exec -s read-only -o target/code-review-result.md '$code-review' < /dev/null
   ```

2. レビュー対象を限定する場合は、コマンドライン上で`$code-review`の後に対象ファイルパスを追記する。

   ```bash
   codex exec -s read-only -o target/code-review-result.md '$code-review
   ただし今回の対象は次のファイルに限定する: path/to/Foo.java' < /dev/null
   ```

3. 完了後、Readツールで`target/code-review-result.md`を読み、MUST・SHOULD・WANTの件数と内容を確認する。
4. チャットにはMUSTの指摘だけを表で報告し、SHOULD/WANTは件数と結果ファイルのパスだけを示す。

## オプションの意味

| オプション       | 意味                                                                                      |
| ---------------- | ----------------------------------------------------------------------------------------- |
| `exec`           | 非対話（ヘッドレス）モード。承認プロンプトが出ず、そのまま完走する                        |
| `-s read-only`   | codexを読み取り専用サンドボックスで動かす。レビュー中のコード改変を物理的に防ぐ           |
| `-o <ファイル>`  | codexの最終メッセージをファイルへ書き出す。`read-only`でもCLI本体が書き込むため出力できる |
| `'$code-review'` | Skillsの`code-review`スキルを記法で読み込み、プロンプトへ展開する。シングルクォートで囲む |

## 注意事項

- `codex`（サブコマンドなし）は対話TUIが起動して応答が返らないため、必ず`codex exec`を使用する。
- **`< /dev/null`を必ず付ける**。付けない場合、codexは`Reading additional input from stdin...`と表示して追加入力を待ち続け、レビューが始まらない。
- プロンプトはシングルクォートで囲む（ダブルクォートではシェルが`$code-review`を未定義変数として展開し消える）。
- `-s read-only`を外さない。レビュー担当がコードを修正することは、いかなる場合も認めない。
- `--dangerously-bypass-approvals-and-sandbox`は使用しない。
- 結果ファイルは`target/`配下（gitignore対象）に出力する。リポジトリへコミットしない。
- codexの実行ログは冗長なため、チャットへ全文を転記しない。結果ファイルの内容に基づいて報告する。
- `.claude/skills/`配下の他スキルと異なり、本スキルは`.agents/skills/`へのシンボリックリンクにしない。Claude Code固有のcodex起動手順を持つため、実ファイルとして管理する。
