package com.example.http.tcp.server.poc.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.http.tcp.server.poc.app.tcp.TcpServer;
import com.example.http.tcp.server.poc.domain.model.tcp.RequestField;
import com.example.http.tcp.server.poc.domain.model.tcp.ResponseField;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.MapPropertySource;

@TestInstance(Lifecycle.PER_CLASS)
class TcpSocketIntegrationTest {

    private AnnotationConfigApplicationContext applicationContext;
    private TcpServer tcpServer;
    private int port;
    private Socket socket;

    @BeforeAll
    void setUpAll() {
        applicationContext = new AnnotationConfigApplicationContext();
        // テスト用プロパティ値。テスト実行環境によって値が変わらないため外部化対象外
        // （ポート0は空きポート自動割当、プールサイズ2・タイムアウト5秒はテスト用固定値）
        Map<String, Object> properties = Map.of(
                "tcp.server.port", "0",
                "tcp.server.threadPoolSize", "2",
                "tcp.server.socketTimeoutMillis", "5000");
        applicationContext.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("testProperties", properties));
        applicationContext.register(TestApplicationConfiguration.class);
        applicationContext.refresh();
        tcpServer = applicationContext.getBean(TcpServer.class);
        port = tcpServer.getPort();
    }

    @AfterAll
    void tearDownAll() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        socket = new Socket("localhost", port);
    }

    @Nested
    class 正常系 {

        @Test
        @DisplayName("Given: 有効なカード照会リクエストが送信されたとき, When: TCPサーバーが処理すると, Then: リターンコード00とマスク済みカード番号を返す")
        void returns00ReturnCodeAndMaskedCardNumberForCardInquiry()
                throws IOException {
            byte[] request = buildCardInquiryRequest("1234567890123456");
            byte[] response = sendRequestAndReceiveResponse(request);
            String returnCode = ResponseField.RETURN_CODE.extract(response);
            String message = ResponseField.MESSAGE.extract(response).trim();
            assertEquals("00", returnCode);
            assertEquals("CARD 123456******3456 VALID", message);
        }

        @Test
        @DisplayName("Given: 有効な決済リクエストが送信されたとき, When: TCPサーバーが処理すると, Then: リターンコード00と承認メッセージを返す")
        void returns00ReturnCodeAndApprovedMessageForPayment()
                throws IOException {
            byte[] request = buildPaymentRequest("1234567890123456", "1000");
            byte[] response = sendRequestAndReceiveResponse(request);
            String returnCode = ResponseField.RETURN_CODE.extract(response);
            String message = ResponseField.MESSAGE.extract(response).trim();
            assertEquals("00", returnCode);
            assertEquals("PAYMENT APPROVED AMOUNT 1000", message);
        }
    }

    @Nested
    class 異常系 {

        @Test
        @DisplayName("Given: 未定義の電文種別を含むリクエストが送信されたとき, When: TCPサーバーが処理すると, Then: リターンコード91「未定義の電文種別」を返す")
        void returns91UndefinedMessageTypeForUnknownMessageType()
                throws IOException {
            byte[] request = buildRequestWithMessageType("0300");
            byte[] response = sendRequestAndReceiveResponse(request);
            String returnCode = ResponseField.RETURN_CODE.extract(response);
            String message = ResponseField.MESSAGE.extract(response).trim();
            assertEquals("91", returnCode);
            assertEquals("UNDEFINED MESSAGE TYPE", message);
        }

        @Test
        @DisplayName("Given: 不正なニブル値を含むBCDフィールドのリクエストが送信されたとき, When: TCPサーバーが処理すると, Then: リターンコード90「電文フォーマット不正」を返す")
        void returns90InvalidMessageForBcdInvalidNibble()
                throws IOException {
            byte[] request = buildCardInquiryRequestWithInvalidNibble();
            byte[] response = sendRequestAndReceiveResponse(request);
            String returnCode = ResponseField.RETURN_CODE.extract(response);
            String message = ResponseField.MESSAGE.extract(response).trim();
            assertEquals("90", returnCode);
            assertEquals("INVALID MESSAGE FORMAT", message);
        }

        @Test
        @DisplayName("Given: 不正なデータ長フィールド値を含むリクエストが送信されたとき, When: TCPサーバーが処理すると, Then: リターンコード90「電文フォーマット不正」を返す")
        void returns90InvalidMessageForDataLengthMismatch()
                throws IOException {
            byte[] request = buildRequestWithDataLength("9999");
            byte[] response = sendRequestAndReceiveResponse(request);
            String returnCode = ResponseField.RETURN_CODE.extract(response);
            String message = ResponseField.MESSAGE.extract(response).trim();
            assertEquals("90", returnCode);
            assertEquals("INVALID MESSAGE FORMAT", message);
        }
    }

    private byte[] buildCardInquiryRequest(String cardNumber) {
        byte[] request = new byte[RequestField.totalLength()];
        RequestField.DATA_LENGTH.write(request, "0014");
        RequestField.MESSAGE_TYPE.write(request, "0100");
        RequestField.CARD_NUMBER.write(request, cardNumber);
        RequestField.AMOUNT.write(request, "0");
        return request;
    }

    private byte[] buildPaymentRequest(String cardNumber, String amount) {
        byte[] request = new byte[RequestField.totalLength()];
        RequestField.DATA_LENGTH.write(request, "0014");
        RequestField.MESSAGE_TYPE.write(request, "0200");
        RequestField.CARD_NUMBER.write(request, cardNumber);
        RequestField.AMOUNT.write(request, amount);
        return request;
    }

    private byte[] buildRequestWithMessageType(String messageType) {
        byte[] request = new byte[RequestField.totalLength()];
        RequestField.DATA_LENGTH.write(request, "0014");
        RequestField.MESSAGE_TYPE.write(request, messageType);
        RequestField.CARD_NUMBER.write(request, "1234567890123456");
        RequestField.AMOUNT.write(request, "0");
        return request;
    }

    private byte[] buildCardInquiryRequestWithInvalidNibble() {
        byte[] request = new byte[RequestField.totalLength()];
        RequestField.DATA_LENGTH.write(request, "0014");
        RequestField.MESSAGE_TYPE.write(request, "0100");
        byte[] invalidCardNumber = {0x12, 0x34, 0x0A, 0x78, (byte) 0x90, 0x12,
                0x34, 0x56};
        System.arraycopy(invalidCardNumber, 0, request,
                RequestField.CARD_NUMBER.getOffset(), 8);
        RequestField.AMOUNT.write(request, "0");
        return request;
    }

    private byte[] buildRequestWithDataLength(String dataLength) {
        byte[] request = new byte[RequestField.totalLength()];
        RequestField.DATA_LENGTH.write(request, dataLength);
        RequestField.MESSAGE_TYPE.write(request, "0100");
        RequestField.CARD_NUMBER.write(request, "1234567890123456");
        RequestField.AMOUNT.write(request, "0");
        return request;
    }

    private byte[] sendRequestAndReceiveResponse(byte[] request)
            throws IOException {
        OutputStream output = socket.getOutputStream();
        InputStream input = socket.getInputStream();
        output.write(request);
        output.flush();
        byte[] response = new byte[ResponseField.totalLength()];
        int totalRead = 0;
        while (totalRead < ResponseField.totalLength()) {
            int read = input.read(response, totalRead,
                    ResponseField.totalLength() - totalRead);
            if (read < 0) {
                break;
            }
            totalRead += read;
        }
        return response;
    }

    @Configuration
    @ComponentScan({
            "com.example.http.tcp.server.poc.app.tcp",
            "com.example.http.tcp.server.poc.domain.service.cardinquiry",
            "com.example.http.tcp.server.poc.domain.service.payment"
    })
    static class TestApplicationConfiguration {

        @Bean
        static PropertySourcesPlaceholderConfigurer
                propertySourcesPlaceholderConfigurer() {
            return new PropertySourcesPlaceholderConfigurer();
        }
    }
}
