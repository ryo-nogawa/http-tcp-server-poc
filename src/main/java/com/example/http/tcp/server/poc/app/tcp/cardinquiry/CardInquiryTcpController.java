package com.example.http.tcp.server.poc.app.tcp.cardinquiry;

import com.example.http.tcp.server.poc.app.tcp.TcpController;
import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import com.example.http.tcp.server.poc.domain.model.tcp.ReturnCode;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;
import com.example.http.tcp.server.poc.domain.service.cardinquiry.CardInquiryService;
import org.springframework.stereotype.Component;

/**
 * カード照会リクエストを処理するTCPコントローラの実装.
 *
 * <p>{@link CardInquiryService}を使用してカード情報を取得し、
 * 成功レスポンスを返す。
 */
@Component
public class CardInquiryTcpController implements TcpController {

    private final CardInquiryService cardInquiryService;

    /**
     * コンストラクタ.
     *
     * @param cardInquiryService カード照会サービス
     */
    public CardInquiryTcpController(CardInquiryService cardInquiryService) {
        this.cardInquiryService = cardInquiryService;
    }

    /**
     * このコントローラが処理する電文種別を返す.
     *
     * @return {@link MessageType#CARD_INQUIRY}
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.CARD_INQUIRY;
    }

    /**
     * カード照会リクエストを処理する.
     *
     * <p>{@link CardInquiryService#inquire(String)}から取得した
     * 照会結果を含むレスポンスを返す。
     *
     * @param request 入力リクエスト
     * @return リターンコード{@link ReturnCode#SUCCESS}を含むレスポンス
     */
    @Override
    public TcpResponse handle(TcpRequest request) {
        String cardNumber = request.getCardNumber();
        String message = cardInquiryService.inquire(cardNumber);
        return new TcpResponse(request.getMessageType(), ReturnCode.SUCCESS,
                message);
    }

}
