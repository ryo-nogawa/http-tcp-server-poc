package com.example.http.tcp.server.poc.app.tcp.cardinquiry;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import com.example.http.tcp.server.poc.domain.model.tcp.ReturnCode;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;
import com.example.http.tcp.server.poc.domain.service.cardinquiry.CardInquiryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CardInquiryTcpControllerTest {

    private CardInquiryTcpController controller;
    private CardInquiryService cardInquiryService;

    @BeforeEach
    void setUp() {
        cardInquiryService = mock(CardInquiryService.class);
        controller = new CardInquiryTcpController(cardInquiryService);
    }

    @Nested
    class GetMessageType {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: CardInquiryTcpControllerが構築されているとき, When: getMessageTypeを実行すると, Then: CARD_INQUIRYが返される")
            void returnsCardInquiryMessageType() {
                MessageType result = controller.getMessageType();
                assertEquals(MessageType.CARD_INQUIRY, result);
            }
        }
    }

    @Nested
    class Handle {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: サービスが有効なメッセージを返しているとき, When: リクエストを処理すると, Then: RETURNコード00と業務メッセージを含むレスポンスが返される")
            void returnsResponseWithSuccessWhenServiceReturnsMessage() {
                when(cardInquiryService.inquire(anyString())).thenReturn(
                        "CARD 123456******3456 VALID");

                TcpRequest request = new TcpRequest("0100",
                        "1234567890123456",
                        "0000");
                TcpResponse response = controller.handle(request);

                assertEquals("0100", response.getMessageType());
                assertEquals(ReturnCode.SUCCESS, response.getReturnCode());
                assertEquals("CARD 123456******3456 VALID", response.getMessage());
            }

            @Test
            @DisplayName("Given: リクエストに特定のカード番号が含まれているとき, When: ハンドラーが処理すると, Then: サービスにそのカード番号が渡される")
            void passesCardNumberToServiceInquiry() {
                String expectedCardNumber = "1234567890123456";
                when(cardInquiryService.inquire(expectedCardNumber)).thenReturn(
                        "CARD 123456******3456 VALID");

                TcpRequest request = new TcpRequest("0100",
                        expectedCardNumber,
                        "0000");
                controller.handle(request);

                verify(cardInquiryService).inquire(expectedCardNumber);
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: サービスが例外をスローするとき, When: リクエストを処理すると, Then: 例外がそのままスローされる")
            void throwsExceptionWhenServiceThrowsException() {
                when(cardInquiryService.inquire(anyString())).thenThrow(
                        new RuntimeException("Service error"));

                TcpRequest request = new TcpRequest("0100",
                        "1234567890123456",
                        "0000");

                assertThrows(RuntimeException.class, () -> controller.handle(request));
            }
        }
    }
}
