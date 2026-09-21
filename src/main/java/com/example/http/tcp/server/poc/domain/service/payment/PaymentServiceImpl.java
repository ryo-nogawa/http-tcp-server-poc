package com.example.http.tcp.server.poc.domain.service.payment;

import org.springframework.stereotype.Service;

/**
 * {@link PaymentService}の実装クラス.
 *
 * <p>決済リクエストを処理し、金額を含む承認メッセージを返す。
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    /**
     * 決済を承認し、金額を含むメッセージを返す.
     *
     * @param cardNumber カード番号（使用しない）
     * @param amount 金額
     * @return 決済承認メッセージ
     */
    @Override
    public String approve(String cardNumber, String amount) {
        return "PAYMENT APPROVED AMOUNT " + amount;
    }

}
