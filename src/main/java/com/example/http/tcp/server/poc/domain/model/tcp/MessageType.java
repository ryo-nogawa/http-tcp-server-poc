package com.example.http.tcp.server.poc.domain.model.tcp;

import java.util.Optional;

/**
 * TCP電文の種別.
 *
 * <p>カード照会・決済など複数の電文種別を定義し、
 * コード値から解決するメソッドを提供する。
 */
public enum MessageType {

    /**
     * カード照会.
     */
    CARD_INQUIRY("0100"),

    /**
     * 決済.
     */
    PAYMENT("0200");

    private final String code;

    /**
     * コンストラクタ.
     *
     * @param code 電文種別を表すコード
     */
    MessageType(String code) {
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

    /**
     * 指定されたコード値から対応する{@link MessageType}を返す.
     *
     * @param code コード値
     * @return 対応する{@link MessageType}を保持するOptional。
     *         コード値が存在しない場合は空のOptionalを返す
     */
    public static Optional<MessageType> fromCode(String code) {
        for (MessageType type : values()) {
            if (type.code.equals(code)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

}
