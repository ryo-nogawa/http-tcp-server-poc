package com.example.http.tcp.server.poc.app.tcp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import com.example.http.tcp.server.poc.domain.model.tcp.RequestField;
import com.example.http.tcp.server.poc.domain.model.tcp.ResponseField;
import com.example.http.tcp.server.poc.domain.model.tcp.ReturnCode;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class TcpConnectionHandlerTest {

    private TcpConnectionHandler handler;
    private TcpMessageCodec mockCodec;
    private TcpDispatcher mockDispatcher;

    @BeforeEach
    void setUp() {
        mockCodec = mock(TcpMessageCodec.class);
        mockDispatcher = mock(TcpDispatcher.class);
        handler = new TcpConnectionHandler(mockCodec, mockDispatcher);
    }

    @Nested
    class 正常系 {

        @Test
        @DisplayName("Given: 電文種別0100の18バイト電文が受信されたとき, When: handleを実行すると, Then: コントローラーの応答が53バイトで書き出される")
        void writesResponseWhenValidCardInquiryRequestReceived()
                throws IOException {
            byte[] requestBytes = buildCardInquiryRequest();
            ByteArrayInputStream input = new ByteArrayInputStream(requestBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Socket socket = mock(Socket.class);
            when(socket.getInputStream()).thenReturn(input);
            when(socket.getOutputStream()).thenReturn(output);

            TcpRequest request = new TcpRequest("0100", "1234567890123456",
                    "1000");
            when(mockCodec.decodeDataLength(any())).thenReturn(14);
            when(mockCodec.decodeRequest(any())).thenReturn(request);

            TcpController mockController = mock(TcpController.class);
            TcpResponse response = new TcpResponse("0100",
                    ReturnCode.SUCCESS, "CARD 123456******3456 VALID");
            when(mockController.handle(request)).thenReturn(response);
            when(mockDispatcher.resolve(MessageType.CARD_INQUIRY))
                    .thenReturn(mockController);

            byte[] responseBytes = new byte[53];
            when(mockCodec.encodeResponse(response)).thenReturn(responseBytes);

            handler.handle(socket);

            assertEquals(53, output.toByteArray().length);
        }
    }

    @Nested
    class 異常系 {

        @Test
        @DisplayName("Given: ヘッダが4バイト未満の受信のとき, When: handleを実行すると, Then: 何も書き出さずに終了する")
        void returnsWithoutWritingWhenHeaderIsIncomplete()
                throws IOException {
            byte[] headerBytes = {0x30, 0x30};
            ByteArrayInputStream input = new ByteArrayInputStream(headerBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Socket socket = mock(Socket.class);
            when(socket.getInputStream()).thenReturn(input);
            when(socket.getOutputStream()).thenReturn(output);

            handler.handle(socket);

            assertEquals(0, output.toByteArray().length);
        }

        @Test
        @DisplayName("Given: データ長が14以外のリクエストが送信されたとき, When: handleを実行すると, Then: リターンコード90の電文が書き出される")
        void writesReturnCode90WhenDataLengthMismatch()
                throws IOException {
            byte[] requestBytes = buildCardInquiryRequest();
            ByteArrayInputStream input = new ByteArrayInputStream(requestBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Socket socket = mock(Socket.class);
            when(socket.getInputStream()).thenReturn(input);
            when(socket.getOutputStream()).thenReturn(output);

            when(mockCodec.decodeDataLength(any())).thenReturn(9999);

            byte[] errorResponse = new byte[53];
            when(mockCodec.encodeResponse(any()))
                    .thenReturn(errorResponse);

            handler.handle(socket);

            ArgumentCaptor<TcpResponse> captor = ArgumentCaptor
                    .forClass(TcpResponse.class);
            verify(mockCodec).encodeResponse(captor.capture());
            assertEquals(ReturnCode.INVALID_MESSAGE,
                    captor.getValue().getReturnCode());
            assertEquals(53, output.toByteArray().length);
        }

        @Test
        @DisplayName("Given: 未定義の電文種別を含むリクエストが送信されたとき, When: handleを実行すると, Then: リターンコード91の電文が書き出される")
        void writesReturnCode91WhenMessageTypeIsUndefined()
                throws IOException {
            byte[] requestBytes = buildRequestWithMessageType("0300");
            ByteArrayInputStream input = new ByteArrayInputStream(requestBytes);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            Socket socket = mock(Socket.class);
            when(socket.getInputStream()).thenReturn(input);
            when(socket.getOutputStream()).thenReturn(output);

            TcpRequest request = new TcpRequest("0300", "1234567890123456",
                    "0");
            when(mockCodec.decodeDataLength(any())).thenReturn(14);
            when(mockCodec.decodeRequest(any())).thenReturn(request);

            when(mockDispatcher.resolve(null)).thenReturn(null);

            byte[] errorResponse = new byte[53];
            when(mockCodec.encodeResponse(any()))
                    .thenReturn(errorResponse);

            handler.handle(socket);

            ArgumentCaptor<TcpResponse> captor = ArgumentCaptor
                    .forClass(TcpResponse.class);
            verify(mockCodec).encodeResponse(captor.capture());
            assertEquals(ReturnCode.UNDEFINED_MESSAGE_TYPE,
                    captor.getValue().getReturnCode());
            assertEquals(53, output.toByteArray().length);
        }
    }

    @Nested
    class Exception {

        @Test
        @DisplayName("Given: getInputStreamがSocketTimeoutExceptionをスローするとき, When: handleを実行すると, Then: 例外が外へ伝播せずソケットがクローズされる")
        void handlesSocketTimeoutExceptionWithoutPropagating()
                throws IOException {
            Socket socket = mock(Socket.class);
            doThrow(new SocketTimeoutException("Socket timeout"))
                    .when(socket).getInputStream();

            assertDoesNotThrow(() -> handler.handle(socket));
            verify(socket).close();
        }

        @Test
        @DisplayName("Given: getInputStreamがIOExceptionをスローするとき, When: handleを実行すると, Then: 例外が外へ伝播せずソケットがクローズされる")
        void handlesIOExceptionWithoutPropagating() throws IOException {
            Socket socket = mock(Socket.class);
            doThrow(new IOException("Connection reset")).when(socket)
                    .getInputStream();

            assertDoesNotThrow(() -> handler.handle(socket));
            verify(socket).close();
        }
    }

    private byte[] buildCardInquiryRequest() {
        byte[] request = new byte[RequestField.totalLength()];
        RequestField.DATA_LENGTH.write(request, "0014");
        RequestField.MESSAGE_TYPE.write(request, "0100");
        RequestField.CARD_NUMBER.write(request, "1234567890123456");
        RequestField.AMOUNT.write(request, "0");
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

}
