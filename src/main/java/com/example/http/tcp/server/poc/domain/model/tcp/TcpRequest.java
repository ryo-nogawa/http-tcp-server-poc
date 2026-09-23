package com.example.http.tcp.server.poc.domain.model.tcp;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TCPリクエスト電文のモデル.
 *
 * <p>受信したバイト列から抽出した電文種別・カード番号・金額を保持する。
 */
@Getter
@AllArgsConstructor
public class TcpRequest {

    /**
     * 電文種別（コード値、未解決）.
     */
    private final String messageType;

    /**
     * カード番号.
     */
    private final String cardNumber;

    /**
     * 金額.
     */
    private final String amount;

}
