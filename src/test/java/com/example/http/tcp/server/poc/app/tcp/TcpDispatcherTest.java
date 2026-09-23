package com.example.http.tcp.server.poc.app.tcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TcpDispatcherTest {

    private TcpDispatcher dispatcher;
    private TcpController cardInquiryController;
    private TcpController paymentController;

    @BeforeEach
    void setUp() {
        cardInquiryController = mock(TcpController.class);
        paymentController = mock(TcpController.class);

        when(cardInquiryController.getMessageType()).thenReturn(
                MessageType.CARD_INQUIRY);
        when(paymentController.getMessageType()).thenReturn(
                MessageType.PAYMENT);
    }

    @Nested
    class Resolve {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: 複数のコントローラが登録されているとき, When: カード照会タイプで解決すると, Then: カード照会コントローラが返される")
            void returnsCardInquiryControllerWhenCardInquiryTypeRequested() {
                dispatcher = new TcpDispatcher(
                        Arrays.asList(cardInquiryController,
                                paymentController));

                TcpController result = dispatcher.resolve(
                        MessageType.CARD_INQUIRY);

                assertEquals(cardInquiryController, result);
            }

            @Test
            @DisplayName("Given: 複数のコントローラが登録されているとき, When: 決済タイプで解決すると, Then: 決済コントローラが返される")
            void returnsPaymentControllerWhenPaymentTypeRequested() {
                dispatcher = new TcpDispatcher(
                        Arrays.asList(cardInquiryController,
                                paymentController));

                TcpController result = dispatcher.resolve(MessageType.PAYMENT);

                assertEquals(paymentController, result);
            }

            @Test
            @DisplayName("Given: 1つのコントローラのみが登録されているとき, When: 対応するタイプで解決すると, Then: そのコントローラが返される")
            void returnsControllerFromSingleControllerList() {
                dispatcher = new TcpDispatcher(
                        Collections.singletonList(cardInquiryController));

                TcpController result = dispatcher.resolve(
                        MessageType.CARD_INQUIRY);

                assertEquals(cardInquiryController, result);
            }
        }

        @Nested
        class 異常系 {

            @Test
            @DisplayName("Given: 対応していないコントローラのみが登録されているとき, When: 未登録の電文種別で解決すると, Then: nullが返される")
            void returnsNullWhenMessageTypeNotRegistered() {
                dispatcher = new TcpDispatcher(
                        Collections.singletonList(cardInquiryController));

                TcpController result = dispatcher.resolve(MessageType.PAYMENT);

                assertNull(result);
            }

            @Test
            @DisplayName("Given: コントローラが登録されていないとき, When: 任意の電文種別で解決すると, Then: nullが返される")
            void returnsNullWhenEmptyControllerList() {
                dispatcher = new TcpDispatcher(Collections.emptyList());

                TcpController result = dispatcher.resolve(
                        MessageType.CARD_INQUIRY);

                assertNull(result);
            }
        }
    }
}
