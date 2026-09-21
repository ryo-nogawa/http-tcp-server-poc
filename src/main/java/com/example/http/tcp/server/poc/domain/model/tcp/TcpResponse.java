package com.example.http.tcp.server.poc.domain.model.tcp;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TCPレスポンス電文のモデル.
 *
 * <p>電文種別・リターンコード・メッセージを保持する。
 */
@Getter
@AllArgsConstructor
public class TcpResponse {

    /**
     * 電文種別（コード値）.
     */
    private final String messageType;

    /**
     * リターンコード.
     */
    private final ReturnCode returnCode;

    /**
     * メッセージ.
     */
    private final String message;

}
