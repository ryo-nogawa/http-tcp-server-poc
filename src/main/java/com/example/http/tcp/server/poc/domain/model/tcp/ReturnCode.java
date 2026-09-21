package com.example.http.tcp.server.poc.domain.model.tcp;

/**
 * TCPレスポンスのリターンコード.
 *
 * <p>処理結果を表す複数のコードを定義する。
 */
public enum ReturnCode {

    /**
     * 成功.
     */
    SUCCESS("00"),

    /**
     * 電文フォーマット不正.
     */
    INVALID_MESSAGE("90"),

    /**
     * 未定義の電文種別.
     */
    UNDEFINED_MESSAGE_TYPE("91");

    private final String code;

    /**
     * コンストラクタ.
     *
     * @param code リターンコード
     */
    ReturnCode(String code) {
        this.code = code;
    }

    /**
     * コード値を返す.
     *
     * @return コード値
     */
    public String getCode() {
        return code;
    }

}
