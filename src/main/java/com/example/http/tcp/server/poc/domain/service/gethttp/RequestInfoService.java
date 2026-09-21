package com.example.http.tcp.server.poc.domain.service.gethttp;

import com.example.http.tcp.server.poc.domain.model.gethttp.RequestInfo;

/**
 * HTTPリクエスト情報を生成するサービスインタフェース.
 *
 * <p>エンドポイントとHTTPメソッドから受信時刻を含めた
 * {@link RequestInfo}を生成するサービスを提供します.
 */
public interface RequestInfoService {

    /**
     * エンドポイントとHTTPメソッドから{@link RequestInfo}を生成する.
     *
     * <p>受信時刻は{@code LocalDateTime.now()}を使用して取得し、
     * 設定されたフォーマットパターンで整形されます.
     *
     * @param endpoint リクエストエンドポイント
     * @param httpMethod HTTPメソッド
     * @return 生成された{@link RequestInfo}
     */
    RequestInfo create(String endpoint, String httpMethod);

}
