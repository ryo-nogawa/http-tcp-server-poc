package com.example.http.tcp.server.poc.app.tcp.payment;

import com.example.http.tcp.server.poc.app.tcp.TcpController;
import com.example.http.tcp.server.poc.domain.model.tcp.MessageType;
import com.example.http.tcp.server.poc.domain.model.tcp.ReturnCode;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpRequest;
import com.example.http.tcp.server.poc.domain.model.tcp.TcpResponse;
import com.example.http.tcp.server.poc.domain.service.payment.PaymentService;
import org.springframework.stereotype.Component;

/**
 * 決済リクエストを処理するTCPコントローラの実装.
 *
 * <p>{@link PaymentService}を使用して決済を承認し、
 * 成功レスポンスを返す。
 */
@Component
public class PaymentTcpController implements TcpController {

    private final PaymentService paymentService;

    /**
     * コンストラクタ.
     *
     * @param paymentService 決済サービス
     */
    public PaymentTcpController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * このコントローラが処理する電文種別を返す.
     *
     * @return {@link MessageType#PAYMENT}
     */
    @Override
    public MessageType getMessageType() {
        return MessageType.PAYMENT;
    }

    /**
     * 決済リクエストを処理する.
     *
     * <p>{@link PaymentService#approve(String, String)}から取得した
     * 承認結果を含むレスポンスを返す。
     *
     * @param request 入力リクエスト
     * @return リターンコード{@link ReturnCode#SUCCESS}を含むレスポンス
     */
    @Override
    public TcpResponse handle(TcpRequest request) {
        String cardNumber = request.getCardNumber();
        String amount = request.getAmount();
        String message = paymentService.approve(cardNumber, amount);
        return new TcpResponse(request.getMessageType(), ReturnCode.SUCCESS,
                message);
    }

}
