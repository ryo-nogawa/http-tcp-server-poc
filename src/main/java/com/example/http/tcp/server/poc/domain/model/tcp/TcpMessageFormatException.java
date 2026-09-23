package com.example.http.tcp.server.poc.domain.model.tcp;

/**
 * TCP電文のフォーマットが不正な場合にスローされる例外.
 *
 * <p>{@code RuntimeException}のサブクラスであり、
 * チェック例外ではなく未チェック例外として扱う。
 */
public class TcpMessageFormatException extends RuntimeException {

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     */
    public TcpMessageFormatException(String message) {
        super(message);
    }

    /**
     * コンストラクタ.
     *
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public TcpMessageFormatException(String message, Throwable cause) {
        super(message, cause);
    }

}
