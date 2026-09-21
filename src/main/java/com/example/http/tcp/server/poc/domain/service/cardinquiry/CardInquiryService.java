package com.example.http.tcp.server.poc.domain.service.cardinquiry;

/**
 * カード照会サービスのインタフェース.
 *
 * <p>カード番号を受け取り、カード情報を照会する。
 */
public interface CardInquiryService {

    /**
     * カード番号から照会結果を返す.
     *
     * @param cardNumber カード番号
     * @return 照会結果メッセージ
     */
    String inquire(String cardNumber);

}
