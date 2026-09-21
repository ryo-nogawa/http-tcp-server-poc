package com.example.http.tcp.server.poc.app.tcp.payment;

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
import com.example.http.tcp.server.poc.domain.service.payment.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class PaymentTcpControllerTest {

    private PaymentTcpController controller;
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = mock(PaymentService.class);
        controller = new PaymentTcpController(paymentService);
    }

    @Nested
    class GetMessageType {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: PaymentTcpControllerが構築されているとき, When: getMessageTypeを実行すると, Then: PAYMENTが返される")
            void returnsPaymentMessageType() {
                MessageType result = controller.getMessageType();
                assertEquals(MessageType.PAYMENT, result);
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
                when(paymentService.approve(anyString(), anyString()))
                        .thenReturn("PAYMENT APPROVED AMOUNT 1000");

                TcpRequest request = new TcpRequest("0200",
                        "1234567890123456",
                        "1000");
                TcpResponse response = controller.handle(request);

                assertEquals("0200", response.getMessageType());
                assertEquals(ReturnCode.SUCCESS, response.getReturnCode());
                assertEquals("PAYMENT APPROVED AMOUNT 1000",
                        response.getMessage());
            }

            @Test
            @DisplayName("Given: リクエストに特定のカード番号と金額が含まれているとき, When: ハンドラーが処理すると, Then: サービスにそれらのパラメータが渡される")
            void passesCardNumberAndAmountToServiceApprove() {
                String expectedCardNumber = "1234567890123456";
                String expectedAmount = "5000";

                when(paymentService.approve(expectedCardNumber,
                        expectedAmount)).thenReturn(
                        "PAYMENT APPROVED AMOUNT 5000");

                TcpRequest request = new TcpRequest("0200",
                        expectedCardNumber,
                        expectedAmount);
                controller.handle(request);

                verify(paymentService).approve(expectedCardNumber, expectedAmount);
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: サービスが例外をスローするとき, When: リクエストを処理すると, Then: 例外がそのままスローされる")
            void throwsExceptionWhenServiceThrowsException() {
                when(paymentService.approve(anyString(), anyString()))
                        .thenThrow(new RuntimeException("Service error"));

                TcpRequest request = new TcpRequest("0200",
                        "1234567890123456",
                        "1000");

                assertThrows(RuntimeException.class, () -> controller.handle(request));
            }
        }
    }
}
