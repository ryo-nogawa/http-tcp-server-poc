package com.example.http.tcp.server.poc.domain.model.gethttp;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HTTPリクエスト情報を表示用に保持するモデル.
 *
 * <p>エンドポイント、HTTPメソッド、受信時刻（整形済み文字列）を保持する。
 * JSPのEL式が{@code record}のアクセサを解決できないため、
 * {@code @Getter}と全項目コンストラクタで実装する。
 */
@Getter
@AllArgsConstructor
public class RequestInfo {

    /**
     * リクエストエンドポイント.
     */
    private final String endpoint;

    /**
     * HTTPメソッド.
     */
    private final String httpMethod;

    /**
     * リクエスト受信時刻（整形済み文字列）.
     */
    private final String receivedAt;

}
