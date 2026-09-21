package com.example.http.tcp.server.poc.app.tcp;

import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import com.example.http.tcp.server.poc.domain.model.tcp.RequestField;
import com.example.http.tcp.server.poc.domain.model.tcp.ReturnCode;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpMessageFormatException;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.springframework.stereotype.Component;

/**
 * TCP接続ごとのリクエスト・レスポンス処理を行うハンドラ.
 *
 * <p>クライアント接続を受け取り、電文の受信・処理・応答を行う。
 */
@Component
public class TcpConnectionHandler {

    private final TcpMessageCodec messageCodec;
    private final TcpDispatcher dispatcher;

    /**
     * コンストラクタ.
     *
     * @param messageCodec 電文のエンコード・デコード用コーデック
     * @param dispatcher 電文種別から対応コントローラを解決するディスパッチャ
     */
    public TcpConnectionHandler(TcpMessageCodec messageCodec,
            TcpDispatcher dispatcher) {
        this.messageCodec = messageCodec;
        this.dispatcher = dispatcher;
    }

    /**
     * クライアント接続を処理する.
     *
     * <p>try-with-resourcesでソケットと入出力ストリームを管理し、
     * 電文の受信・処理・応答を行う。
     *
     * @param socket クライアントのソケット
     * @param timeoutMillis ソケットのタイムアウト時間（ミリ秒）
     */
    public void handle(Socket socket, long timeoutMillis) {
        try (socket) {
            InputStream input = socket.getInputStream();
            OutputStream output = socket.getOutputStream();

            byte[] header = new byte[4];
            int bytesRead = readFully(input, header, 4);
            if (bytesRead < 4) {
                return;
            }

            try {
                int dataLength = messageCodec.decodeDataLength(header);

                // 固定長プロトコルのため、データ長が不正な場合に本体の読み込みで
                // ソケットタイムアウトまでブロックしたり、負値でNegativeArraySizeException
                // が発生するのを防ぐ
                if (dataLength != RequestField.totalLength() - 4) {
                    throw new TcpMessageFormatException(
                            "Data length mismatch: expected "
                                    + (RequestField.totalLength() - 4)
                                    + ", got " + dataLength);
                }

                byte[] body = new byte[dataLength];
                bytesRead = readFully(input, body, dataLength);
                if (bytesRead < dataLength) {
                    return;
                }

                byte[] message = new byte[4 + dataLength];
                System.arraycopy(header, 0, message, 0, 4);
                System.arraycopy(body, 0, message, 4, dataLength);

                TcpRequest request = messageCodec.decodeRequest(message);

                MessageType messageType = MessageType.fromCode(
                        request.getMessageType()).orElse(null);

                TcpResponse response;
                if (messageType == null) {
                    // 電文種別が未定義のため、電文種別をそのまま返しつつ
                    // リターンコード91で応答
                    response = new TcpResponse(request.getMessageType(),
                            ReturnCode.UNDEFINED_MESSAGE_TYPE,
                            "UNDEFINED MESSAGE TYPE");
                } else {
                    TcpController controller = dispatcher.resolve(messageType);
                    if (controller != null) {
                        response = controller.handle(request);
                    } else {
                        response = new TcpResponse(request.getMessageType(),
                                ReturnCode.UNDEFINED_MESSAGE_TYPE,
                                "UNDEFINED MESSAGE TYPE");
                    }
                }

                byte[] responseBytes = messageCodec.encodeResponse(response);
                output.write(responseBytes);
                output.flush();

            } catch (TcpMessageFormatException e) {
                // 電文フォーマットが不正のため、電文種別を0000として
                // リターンコード90で応答
                e.printStackTrace();
                TcpResponse errorResponse = new TcpResponse("0000",
                        ReturnCode.INVALID_MESSAGE,
                        "INVALID MESSAGE FORMAT");
                byte[] responseBytes = messageCodec.encodeResponse(errorResponse);
                output.write(responseBytes);
                output.flush();
            }

        } catch (SocketTimeoutException e) {
            // タイムアウトは正常な接続終了のため、スタックトレースを記録した上で
            // 応答せずクローズ
            e.printStackTrace();
        } catch (IOException e) {
            // IO例外（接続切断など）は正常な接続終了のため、スタックトレースを記録した上で
            // 応答せずクローズ
            e.printStackTrace();
        }
    }

    /**
     * 入力ストリームから指定バイト数を読み切る.
     *
     * <p>1回の{@code read()}で全量が届かない場合に複数回読み込みを行う。
     *
     * @param input 入力ストリーム
     * @param buffer 読み込みバッファ
     * @param length 読み込むバイト数
     * @return 実際に読み込んだバイト数
     * @throws IOException 読み込み中の例外
     */
    private int readFully(InputStream input, byte[] buffer, int length)
            throws IOException {
        int totalRead = 0;
        while (totalRead < length) {
            int read = input.read(buffer, totalRead, length - totalRead);
            if (read < 0) {
                break;
            }
            totalRead += read;
        }
        return totalRead;
    }

}
