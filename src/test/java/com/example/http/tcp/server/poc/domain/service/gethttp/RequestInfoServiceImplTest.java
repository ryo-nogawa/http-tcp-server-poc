package com.example.http.tcp.server.poc.domain.service.gethttp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.http.tcp.server.poc.domain.model.gethttp.RequestInfo;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link RequestInfoServiceImpl}の単体テスト。
 */
class RequestInfoServiceImplTest {

    @Nested
    class 正常系 {

        @Test
        @DisplayName("Given: 有効なフォーマットパターンが与えられたとき, "
                + "When: createメソッドを実行すると, "
                + "Then: requestInfoが返され各項目が正しく設定されている")
        void returnsRequestInfoWithValidFormattedDateWhenValidFormatPatternIsSet() {
            String formatPattern = "yyyy-MM-dd HH:mm:ss.SSS";
            RequestInfoService service = new RequestInfoServiceImpl(formatPattern);

            String endpoint = "/get-http";
            String httpMethod = "GET";
            RequestInfo result = service.create(endpoint, httpMethod);

            assertNotNull(result);
            assertEquals(endpoint, result.getEndpoint());
            assertEquals(httpMethod, result.getHttpMethod());
            assertNotNull(result.getReceivedAt());

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            // パターンに合致することを確認
            try {
                LocalDateTime.parse(result.getReceivedAt(), formatter);
            } catch (Exception e) {
                throw new AssertionError("receivedAtがパターンに合致しません: "
                        + result.getReceivedAt(), e);
            }
        }

        @Test
        @DisplayName("Given: 異なるフォーマットパターンが与えられたとき, "
                + "When: createメソッドを実行すると, "
                + "Then: そのパターンに従ってreceivedAtが整形されている")
        void returnsRequestInfoWithDifferentFormatPattern() {
            String formatPattern = "yyyy/MM/dd HH:mm:ss";
            RequestInfoService service = new RequestInfoServiceImpl(formatPattern);

            RequestInfo result = service.create("/endpoint", "POST");

            assertNotNull(result);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(formatPattern);
            try {
                LocalDateTime.parse(result.getReceivedAt(), formatter);
            } catch (Exception e) {
                throw new AssertionError("receivedAtがパターンに合致しません: "
                        + result.getReceivedAt(), e);
            }
        }
    }

    @Nested
    class 異常系 {

        @Test
        @DisplayName("Given: 不正なフォーマットパターンが与えられたとき, "
                + "When: RequestInfoServiceImplのコンストラクタを実行すると, "
                + "Then: DateTimeFormatterが例外をスロー")
        void throwsExceptionWhenInvalidFormatPatternIsSet() {
            String invalidPattern = "invalid pattern !!!";

            assertThrows(IllegalArgumentException.class, () -> {
                new RequestInfoServiceImpl(invalidPattern);
            });
        }
    }

}
