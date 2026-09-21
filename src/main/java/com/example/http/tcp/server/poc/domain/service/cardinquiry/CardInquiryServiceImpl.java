package com.example.http.tcp.server.poc.domain.service.cardinquiry;

import org.springframework.stereotype.Service;

/**
 * {@link CardInquiryService}の実装クラス.
 *
 * <p>カード番号をマスクし、カード有効性メッセージを返す。
 */
@Service
public class CardInquiryServiceImpl implements CardInquiryService {

    /**
     * カード番号を照会し、マスク済みの番号を含むメッセージを返す.
     *
     * <p>カード番号は上位6桁と下位4桁を露出させ、
     * 中間の6桁をアスタリスクでマスクする。
     *
     * @param cardNumber カード番号（16桁のBCD符号化値）
     * @return カード情報メッセージ
     */
    @Override
    public String inquire(String cardNumber) {
        String maskedCardNumber = cardNumber.substring(0, 6) + "******"
                + cardNumber.substring(12);
        return "CARD " + maskedCardNumber + " VALID";
    }

}
