package com.example.http.tcp.server.poc.domain.service.cardinquiry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CardInquiryServiceImplTest {

    private CardInquiryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CardInquiryServiceImpl();
    }

    @Nested
    class Inquire {

        @Nested
        class 正常系 {

            @Test
            @DisplayName("Given: 16桁のカード番号が与えられたとき, When: inquireを実行すると, Then: マスク済みカード番号と有効メッセージを返す")
            void returnsMaskedCardNumberWithValidMessage() {
                String cardNumber = "1234567890123456";
                String result = service.inquire(cardNumber);
                assertEquals("CARD 123456******3456 VALID", result);
            }

            @Test
            @DisplayName("Given: 全ゼロのカード番号が与えられたとき, When: inquireを実行すると, Then: 先頭6桁がゼロのままマスクされる")
            void masksMiddle6DigitsWithAsterisks() {
                String cardNumber = "0000000000000000";
                String result = service.inquire(cardNumber);
                assertEquals("CARD 000000******0000 VALID", result);
            }

            @Test
            @DisplayName("Given: カード番号が与えられたとき, When: inquireを実行すると, Then: 先頭6桁と末尾4桁が保持される")
            void preservesFirst6DigitsAndLast4Digits() {
                String cardNumber = "1111112222223333";
                String result = service.inquire(cardNumber);
                assertEquals("CARD 111111******3333 VALID", result);
            }

            @Test
            @DisplayName("Given: 別のカード番号が与えられたとき, When: inquireを実行すると, Then: 正しいメッセージ形式で返される")
            void messageIncludesCorrectFormat() {
                String cardNumber = "9876543210987654";
                String result = service.inquire(cardNumber);
                assertEquals("CARD 987654******7654 VALID", result);
            }
        }
    }
}
