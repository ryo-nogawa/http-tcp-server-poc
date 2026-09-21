# http-tpc-server-poc

## 概要

HTTPリクエストとTCP Socketの両方を同一ポートで待ち受けるサーバーのPoCです。

- **目的**: 1つのサーバーでHTTP通信とTCP Socket通信の両方を受信できることを検証する
- **スコープ**: ドメイン領域（サービス・ビュー）の実装は簡素なもので良い
- **完了条件**: 受信した通信がHTTPかTCP Socketかを、レスポンスのHTML上で判別できること

## 技術スペック

- Javaバージョン: 21
- Terasolunaバージョン: 5.10.1.RELEASE
- Mavenバージョン: 3.9.11
- Tomcatバージョン: 10.1.60

## 起動方法

Spring Bootのような組み込み（Embedded）Tomcatは使用せず、Tomcatを単体でインストールし、そのインストール先ディレクトリを`CATALINA_HOME`環境変数で指定した上で、WARファイルをデプロイして起動します。

事前にシェルの設定ファイル（`.zshrc`など）で、Tomcatのインストール先を`CATALINA_HOME`に設定しておいてください。

```
export CATALINA_HOME=/path/to/your/tomcat
```

1. WARファイルをビルドする

   ```
   mvn clean package
   ```

   `target/http-tcp-server-poc.war` が生成されます。

2. 外部Tomcatの`webapps`ディレクトリにWARファイルを配置する

   ```
   cp target/http-tcp-server-poc.war ${CATALINA_HOME}/webapps/
   ```

3. 外部Tomcatを起動する

   ```
   ${CATALINA_HOME}/bin/startup.sh
   ```

4. 停止する場合

   ```
   ${CATALINA_HOME}/bin/shutdown.sh
   ```

## 実装ルール

開発フローはTDD（テスト駆動開発）に従い、以下のRed→Green→Refactorのサイクルで進める。

1. **Red**: これから実装する機能に対応するテストコードを先に書き、テストが失敗（Red）することを確認する
2. **Green**: テストをパスさせるための最小限の実装を行い、テストが成功（Green）することを確認する
3. **Refactor**: テストが通ったままの状態を保ちながらコードをリファクタリングし、完成させる

- 静的解析ツールとしてcheckstyleを毎回実行する

コーディング規約は以下を参照してください。

- @.claude/rules/error-handling.md
- @.claude/rules/javadoc.md
- @.claude/rules/testing.md
