package com.example.http.tcp.server.poc.app.tcp;

import com.example.http.tcp.server.poc.domain.model.tcp.RequestField;
import com.example.http.tcp.server.poc.domain.model.tcp.ResponseField;
import com.example.http.tcp.server.poc.domain.model.tcp.ReturnCode;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpMessageFormatException;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;
import org.springframework.stereotype.Component;

/**
 * TCP電文のエンコード・デコードを行うコーデック.
 *
 * <p>バイト列とモデルオブジェクト間の相互変換を実装する。
 */
@Component
public class TcpMessageCodec {

    /**
     * ヘッダ（先頭4バイト）からデータ長を取得する.
     *
     * <p>{@link RequestField#DATA_LENGTH}を使用してデコードし、
     * 数値化できない場合は{@link TcpMessageFormatException}をスローする。
     *
     * @param header デコード対象のヘッダ（4バイト）
     * @return データ長
     * @throws TcpMessageFormatException デコード失敗時
     */
    public int decodeDataLength(byte[] header) {
        try {
            String dataLengthStr = RequestField.DATA_LENGTH.extract(header);
            return Integer.parseInt(dataLengthStr.trim());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new TcpMessageFormatException(
                    "Failed to parse data length", e);
        }
    }

    /**
     * 18バイトの電文バイト列を{@link TcpRequest}に変換する.
     *
     * <p>電文長が{@link RequestField#totalLength()}と一致しない場合は
     * {@link TcpMessageFormatException}をスローする。
     *
     * @param message デコード対象の電文バイト列
     * @return デコード済みの{@link TcpRequest}
     * @throws TcpMessageFormatException デコード失敗時
     */
    public TcpRequest decodeRequest(byte[] message) {
        if (message.length != RequestField.totalLength()) {
            throw new TcpMessageFormatException(
                    "Message length mismatch: expected "
                    + RequestField.totalLength() + ", got " + message.length);
        }
        String messageType = RequestField.MESSAGE_TYPE.extract(message);
        String cardNumber = RequestField.CARD_NUMBER.extract(message);
        String amount = RequestField.AMOUNT.extract(message);
        return new TcpRequest(messageType, cardNumber, amount);
    }

    /**
     * {@link TcpResponse}を53バイトのレスポンス電文に変換する.
     *
     * <p>データ長項目は右スペース埋めのTXT属性であるため、
     * {@code String.format("%04d", ...)}で4桁ゼロ埋め済みの値を使用する。
     * データ長項目には自身の4バイトを除いた{@link ResponseField#totalLength()} - 4 = 49を設定する。
     *
     * @param response エンコード対象の{@link TcpResponse}
     * @return エンコード済みの電文バイト列（53バイト）
     * @throws TcpMessageFormatException エンコード失敗時
     */
    public byte[] encodeResponse(TcpResponse response) {
        byte[] result = new byte[ResponseField.totalLength()];
        String dataLength = String.format("%04d", ResponseField.totalLength() - 4);
        ResponseField.DATA_LENGTH.write(result, dataLength);
        ResponseField.MESSAGE_TYPE.write(result, response.getMessageType());
        ResponseField.RETURN_CODE.write(result, response.getReturnCode().getCode());
        ResponseField.MESSAGE.write(result, response.getMessage());
        return result;
    }

}
