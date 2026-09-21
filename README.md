# http-tcp-server-poc

HTTPリクエストとTCP Socketの両方を同一ポートで待ち受けるサーバーのPoCです。

詳細な目的・スコープ・完了条件、コーディング規約や実装ルールは [AGENTS.md](./AGENTS.md) を参照してください。本READMEでは、プロジェクトを触り始めるにあたって必要な実用的な情報をまとめます。

## 動作確認方法

[AGENTS.md](./AGENTS.md) の手順でWARファイルをビルド・デプロイし、外部Tomcatを起動した後、以下のURLにアクセスします。

```
http://localhost:8080/http-tcp-server-poc/
```

"Hello world!" とサーバーの現在時刻が表示されます。

> **TODO**: この画面は開発初期段階の暫定実装（TERASOLUNAブランクプロジェクトのWelcome画面）です。今後、本来の目的であるHTTP/TCP Socket判別機能を実装する過程で削除される予定です。

## プロジェクト構成

`src/main/java` 以下は、以下の親パッケージで構成されています（配下の詳細な構成は今後変更される可能性があるため、ここでは役割のみ記載します）。

| パッケージ | 役割 |
| --- | --- |
| `app` | 画面・アプリケーション層（Controllerなど） |
| `config` | Spring MVC・Spring Security等の設定クラス |
| `domain` | ドメイン領域（model / repository / service）。現状は雛形のみ |

## 技術スタック・依存ライブラリ

[AGENTS.md](./AGENTS.md) に記載の技術スペック（Java 21 / Terasoluna 5.10.1.RELEASE / Maven 3.9.11 / Tomcat 10.1.60）に加え、以下のライブラリを使用しています。

- テスト: JUnit, Mockito, spring-test, Selenium, WebDriverManager
- パッケージング: war（ビルドすると `target/http-tcp-server-poc.war` が生成されます）

## 事前準備の補足

`pom.xml` で `maven-toolchains-plugin` によりJDK21のtoolchainが要求されます。手元の環境でビルドが失敗する場合は、`~/.m2/toolchains.xml` にJDK21のtoolchain設定が必要です。

```xml
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>21</version>
    </provides>
    <configuration>
      <jdkHome>/path/to/your/jdk21</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

## テストの実行方法

```
mvn test
```

テストの分類（単体テスト・結合テスト）や必須の確認観点は [.claude/rules/testing.md](./.claude/rules/testing.md) を参照してください。
