package com.example.http.tcp.server.poc.domain.service.payment;

/**
 * 決済サービスのインタフェース.
 *
 * <p>カード番号と金額から決済を承認する。
 */
public interface PaymentService {

    /**
     * 決済リクエストを承認し、結果メッセージを返す.
     *
     * @param cardNumber カード番号
     * @param amount 金額
     * @return 決済結果メッセージ
     */
    String approve(String cardNumber, String amount);

}
