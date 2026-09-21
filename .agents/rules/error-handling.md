# 例外処理

- PoCであるため、過剰なエラーハンドリングは不要
- チェック例外（`Exception`のサブクラスのうち`RuntimeException`系を除くもの）が発生しうるメソッドのみ`try-catch`で囲む
- リソース系（`Closeable`/`AutoCloseable`を実装するクラスなど）の例外が発生しうる場合は、`try-with-resources`を使用する
- 例外を捕捉した際は、必ずスタックトレースを出力する
