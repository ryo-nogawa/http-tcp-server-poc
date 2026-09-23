package com.example.http.tcp.server.poc.app.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.http.tcp.server.poc.domain.model.tcp.RequestField;
import com.example.http.tcp.server.poc.domain.model.tcp.ResponseField;
import com.example.http.tcp.server.poc.domain.model.tcp.ReturnCode;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpMessageFormatException;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TcpMessageCodecTest {

    private TcpMessageCodec codec;

    @BeforeEach
    void setUp() {
        codec = new TcpMessageCodec();
    }

    @Nested
    class DecodeDataLength {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: ヘッダに有効な数字が含まれているとき, When: decodeDataLengthを実行すると, Then: その数値が返される")
            void returnsIntegerWhenHeaderContainsValidNumber() {
                byte[] header = {0x30, 0x31, 0x30, 0x34};
                int result = codec.decodeDataLength(header);
                assertEquals(104, result);
            }

            @Test
            @DisplayName("Given: ヘッダが0000を含むとき, When: decodeDataLengthを実行すると, Then: 0が返される")
            void returnsZeroWhenHeaderContainsZero() {
                byte[] header = {0x30, 0x30, 0x30, 0x30};
                int result = codec.decodeDataLength(header);
                assertEquals(0, result);
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: ヘッダが非数字文字を含むとき, When: decodeDataLengthを実行すると, Then: TcpMessageFormatExceptionがスローされる")
            void throwsExceptionWhenHeaderContainsNonNumericCharacters() {
                byte[] header = {0x41, 0x42, 0x43, 0x44};
                assertThrows(TcpMessageFormatException.class, () -> {
                    codec.decodeDataLength(header);
                });
            }
        }
    }

    @Nested
    class DecodeRequest {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: 正確に18バイトのリクエスト電文が与えられたとき, When: decodeRequestを実行すると, Then: 各フィールドが正しくデコードされたTcpRequestが返される")
            void returnsRequestWhen18BytesProvided() {
                byte[] message = new byte[18];
                System.arraycopy(new byte[]{0x30, 0x30, 0x31, 0x34}, 0,
                        message, 0, 4);
                System.arraycopy(new byte[]{0x01, 0x00}, 0, message, 4, 2);
                System.arraycopy(
                        new byte[]{0x12, 0x34, 0x56, 0x78, (byte) 0x90, 0x12,
                                0x34, 0x56},
                        0, message, 6, 8);
                System.arraycopy(new byte[]{0x00, 0x00, 0x03, (byte) 0xE8},
                        0, message, 14, 4);

                TcpRequest request = codec.decodeRequest(message);

                assertEquals("0100", request.getMessageType());
                assertEquals("1234567890123456", request.getCardNumber());
                assertEquals("1000", request.getAmount());
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: 18バイト未満の電文が与えられたとき, When: decodeRequestを実行すると, Then: TcpMessageFormatExceptionがスローされる")
            void throwsExceptionWhenMessageLengthIsShorterThan18() {
                byte[] message = new byte[17];
                assertThrows(TcpMessageFormatException.class, () -> {
                    codec.decodeRequest(message);
                });
            }

            @Test
            @DisplayName("Given: 18バイトを超える電文が与えられたとき, When: decodeRequestを実行すると, Then: TcpMessageFormatExceptionがスローされる")
            void throwsExceptionWhenMessageLengthIsLongerThan18() {
                byte[] message = new byte[19];
                assertThrows(TcpMessageFormatException.class, () -> {
                    codec.decodeRequest(message);
                });
            }
        }
    }

    @Nested
    class EncodeResponse {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: TcpResponseが与えられたとき, When: encodeResponseを実行すると, Then: 53バイトの電文が返される")
            void returns53BytesWhenResponseProvided() {
                TcpResponse response = new TcpResponse("0100",
                        ReturnCode.SUCCESS,
                        "TEST MESSAGE");

                byte[] result = codec.encodeResponse(response);

                assertEquals(53, result.length);
            }

            @Test
            @DisplayName("Given: レスポンスが与えられたとき, When: encodeResponseを実行すると, Then: データ長フィールドが4桁ゼロ埋めで正しくエンコードされる")
            void encodesDataLengthAs4DigitZeroPadded() {
                TcpResponse response = new TcpResponse("0100",
                        ReturnCode.SUCCESS,
                        "");

                byte[] result = codec.encodeResponse(response);

                byte[] expectedDataLength = {0x30, 0x30, 0x34, 0x39};
                for (int i = 0; i < 4; i++) {
                    assertEquals(expectedDataLength[i], result[i]);
                }
            }

            @Test
            @DisplayName("Given: 異なる電文種別を含むレスポンスが与えられたとき, When: encodeResponseを実行すると, Then: 電文種別フィールドが正しくエンコードされる")
            void encodesMessageTypeAsProvidedInResponse() {
                TcpResponse response = new TcpResponse("0200",
                        ReturnCode.SUCCESS,
                        "");

                byte[] result = codec.encodeResponse(response);

                byte[] decodedMessageType = new byte[2];
                System.arraycopy(result, 4, decodedMessageType, 0, 2);
                String messageType = ResponseField.MESSAGE_TYPE
                        .extract(result);
                assertEquals("0200", messageType);
            }

            @Test
            @DisplayName("Given: 異なるリターンコードを含むレスポンスが与えられたとき, When: encodeResponseを実行すると, Then: リターンコードフィールドが正しくエンコードされる")
            void encodesReturnCodeCorrectly() {
                TcpResponse response = new TcpResponse("0100",
                        ReturnCode.INVALID_MESSAGE,
                        "");

                byte[] result = codec.encodeResponse(response);

                String returnCode = ResponseField.RETURN_CODE.extract(result);
                assertEquals("90", returnCode);
            }

            @Test
            @DisplayName("Given: TcpResponseをエンコードしてデコードしたとき, When: ラウンドトリップを実行すると, Then: 各フィールドの値が保持される")
            void encodeDecodeRoundTrip() {
                String messageTypeCode = "0100";
                ReturnCode returnCode = ReturnCode.SUCCESS;
                String message = "SUCCESS MESSAGE";

                TcpResponse originalResponse = new TcpResponse(messageTypeCode,
                        returnCode,
                        message);

                byte[] encoded = codec.encodeResponse(originalResponse);

                String decodedMessageType = ResponseField.MESSAGE_TYPE
                        .extract(encoded);
                String decodedReturnCode = ResponseField.RETURN_CODE
                        .extract(encoded);
                String decodedMessage = ResponseField.MESSAGE.extract(encoded)
                        .trim();

                assertEquals(messageTypeCode, decodedMessageType);
                assertEquals(returnCode.getCode(), decodedReturnCode);
                assertEquals(message, decodedMessage);
            }
        }
    }
}
