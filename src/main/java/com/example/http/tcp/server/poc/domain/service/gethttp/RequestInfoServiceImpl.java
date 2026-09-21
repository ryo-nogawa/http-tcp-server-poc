package com.example.http.tcp.server.poc.domain.service.gethttp;

import com.example.http.tcp.server.poc.domain.model.gethttp.RequestInfo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * {@link RequestInfoService}の実装クラス.
 *
 * <p>受信時刻を設定ファイルで指定されたフォーマットパターンで整形して
 * {@link RequestInfo}を生成する。
 */
@Service
public class RequestInfoServiceImpl implements RequestInfoService {

    private final DateTimeFormatter dateTimeFormatter;

    /**
     * コンストラクタ.
     *
     * <p>設定ファイルから取得したフォーマットパターン文字列から
     * {@link DateTimeFormatter}を生成する。
     *
     * @param formatPattern フォーマットパターン文字列
     *        （例: {@code "yyyy-MM-dd HH:mm:ss.SSS"}）
     */
    public RequestInfoServiceImpl(
            @Value("${http.request.receivedAt.format}") String formatPattern) {
        this.dateTimeFormatter = DateTimeFormatter.ofPattern(formatPattern);
    }

    /**
     * エンドポイントとHTTPメソッドから{@link RequestInfo}を生成する.
     *
     * <p>現在の日時を取得し、設定されたフォーマットパターンで整形してから
     * {@link RequestInfo}を生成します。
     *
     * @param endpoint リクエストエンドポイント
     * @param httpMethod HTTPメソッド
     * @return 生成された{@link RequestInfo}
     */
    @Override
    public RequestInfo create(String endpoint, String httpMethod) {
        LocalDateTime now = LocalDateTime.now();
        String formattedTime = now.format(dateTimeFormatter);
        return new RequestInfo(endpoint, httpMethod, formattedTime);
    }

}
