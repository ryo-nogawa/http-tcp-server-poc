package com.example.http.tcp.server.poc.app.tcp;

import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;

/**
 * TCP電文を処理するコントローラのインタフェース.
 *
 * <p>複数の電文種別に対応する実装クラスを定義する基盤。
 */
public interface TcpController {

    /**
     * このコントローラが処理する電文種別を返す.
     *
     * @return 処理対象の{@link MessageType}
     */
    MessageType getMessageType();

    /**
     * TCPリクエストを処理し、レスポンスを生成する.
     *
     * @param request 入力リクエスト
     * @return 生成されたレスポンス
     */
    TcpResponse handle(TcpRequest request);

}
