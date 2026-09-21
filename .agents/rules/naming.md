# 命名規則

## 対象別の規則

| 対象   | 規則             | 例                 |
| ------ | ---------------- | ------------------ |
| クラス | UpperCamelCase   | `RequestParser`    |
| メソッド | lowerCamelCase | `parseRequest`     |
| 変数   | lowerCamelCase   | `requestBody`      |
| 定数   | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT`  |
| パッケージ | 全て小文字、単語区切りなし | `welcome` |
| インタフェース | UpperCamelCase（役割を表す名前とする） | `UserRepository` |
| 実装クラス | インタフェース名 + `Impl` | `UserRepositoryImpl` |
| Enum型 | UpperCamelCase、単数形 | `Status` |
| Enum定数 | UPPER_SNAKE_CASE | `ACTIVE`, `PENDING_APPROVAL` |

## パッケージ構成

レイヤードアーキテクチャに従い、以下の技術層をトップレベルのパッケージとする。

- `app`: 画面・APIのエントリポイント（Controllerなど）
- `domain.model`: ドメインモデル（Entity、Formなど）
- `domain.repository`: データアクセス
- `domain.service`: ビジネスロジック
- `config`: 設定クラス（`config.app`、`config.web`など用途別にサブパッケージを分ける）

機能を追加する場合は、各層の配下に機能名のサブパッケージを作成する。

### 記載例

```
com.example.http.tcp.server.poc
├── app
│   └── welcome
│       └── WelcomeController.java
├── domain
│   ├── model
│   │   └── welcome
│   ├── repository
│   │   └── welcome
│   └── service
│       └── welcome
└── config
    ├── app
    └── web
```
